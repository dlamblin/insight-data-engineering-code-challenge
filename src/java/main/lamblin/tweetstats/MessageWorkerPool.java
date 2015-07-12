package lamblin.tweetstats;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import com.beust.jcommander.internal.Sets;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lamblin.common.runningmedian.RunningMedian;

/**
 * Each message added to this worker pool is run by a {@link Executors#newWorkStealingPool()}.
 * The parent thread accumulates counts as totals of each word from messages added with
 * {@link #addMessage(String)}. It also retains a sorted set of words for outputting words in order.
 * Then the worker passes each message's count of unique words, via a {@link ConcurrentLinkedQueue}
 * to the runnable worker {@link RunningMedianTask}, which is started by
 * {@link #startUniqueWordsRunningMedian(RunningMedian, PrintStream)}.
 *
 * @author Daniel Lamblin
 */
public class MessageWorkerPool {

  private static final TimeUnit poolTimeoutUnits = TimeUnit.SECONDS;
  private static final long poolTimeout = 5;
  private static final Splitter splitter = Splitter.on(CharMatcher.BREAKING_WHITESPACE)
      .trimResults().omitEmptyStrings();
  private final ExecutorService pool;
  private final Multiset<String> words = ConcurrentHashMultiset.create();
  private final ConcurrentSkipListSet<String> sortedWords = new ConcurrentSkipListSet<>();
  private final ConcurrentLinkedQueue<SequencedCount> queue;

  private int sequence = 0;

  public MessageWorkerPool(ConcurrentLinkedQueue<SequencedCount> queue) {
    this.queue = queue;
    pool = Executors.newWorkStealingPool();
  }

  /**
   * Adds messages with words to be counted. You cannot add messages after calling either
   * {@link #getCounts} or {@link #getSortedWords}.
   *
   * @param message the message for which to increment its words' counts
   */
  public void addMessage(String message) {
    pool.execute(new WordCounter(sequence++, message));
  }

  /**
   * The total number of unique words added.
   *
   * @return the total number of unique words
   */
  public int getSize() {
    return words.size();
  }

  /**
   * How many of a given word were counted.
   *
   * @param word being counted
   * @return The number of times the word being counted was seen in all messages
   */
  public int getCount(String word) {
    return words.count(word);
  }

  /**
   * The unordered entry set of words and their counts. Getting this will terminate the
   * accumulator's thread pool and disable the {@link #addMessage(String)} method.
   *
   * @return a set of words and their counts
   */
  public Set<Multiset.Entry<String>> getCounts() {
    terminatePool();
    return words.entrySet();
  }

  /**
   * The ordered set of words counted. It does not include their counts. Getting this will terminate
   * the accumulator's thread pool and disable the {@link #addMessage(String)} method.
   *
   * @return a set of the words in natural order
   */
  public SortedSet<String> getSortedWords() {
    terminatePool();
    return sortedWords;
  }

  private void terminatePool() {
    if (!pool.isTerminated()) {
      try {
        pool.shutdown();
        pool.awaitTermination(poolTimeout, poolTimeoutUnits);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Sets one long-running {@link Runnable} to gather and resequence the counts of unique words out
   * of the queue and output the current running median of these counts to a stream with receipt of
   * each.
   *
   * @param runningMedian the running median implementation to use to update the median
   * @param medianUniqueWordsOutput the {@link PrintStream} to output the running median to
   */
  public void startUniqueWordsRunningMedian(RunningMedian<Integer> runningMedian,
                                            PrintStream medianUniqueWordsOutput) {
    pool.execute(new RunningMedianTask(runningMedian, medianUniqueWordsOutput));
  }

  /**
   * Adds a message's words to the counting and sorted sets and queue its unique words count for the
   * RunningMedian task.
   */
  private class WordCounter implements Runnable {

    private final int sequence;
    private final String message;

    /**
     * As each message comes in, it will be processed with it's sequence by this runnable, which
     * may not complete the work quite in sequence, depending on the current state of the pool.
     *
     * @param sequence the monotonically increasing sequence int which may roll over
     * @param message the message to be processed
     */
    public WordCounter(int sequence, String message) {
      this.sequence = sequence;
      this.message = message;
    }

    /**
     * Splits the message on {@link CharMatcher#BREAKING_WHITESPACE} into words and both counts the
     * totals across messages, and sends the unique number in this message, with the sequence, to
     * the queue for the running median.
     */
    @Override
    public void run() {
      Set<String> unique = Sets.newHashSet();
      for (String word : splitter.split(message)) {
        unique.add(word);
        words.add(word);
        sortedWords.add(word);
      }
      queue.add(SequencedCount.create(sequence, unique.size()));
    }
  }

  /**
   * Dequeue {@link SequencedCount} objects and output the running median of the counts in sequence.
   */
  private class RunningMedianTask implements Runnable {

    private final RunningMedian<Integer> runningMedian;
    private final PrintStream medianUniqueWordsOutput;

    /**
     * Will output a running median from the implementing class given to the {@link PrintStream}
     * provided. It will buffer counts from the queue who came out of the expected sequence order.
     *
     * @param runningMedian the implementing class for tracking the running median of unique words
     * @param medianUniqueWordsOutput the print stream to which the median will be written as each
     *                                message comes in from the queue
     */
    public RunningMedianTask(RunningMedian<Integer> runningMedian,
                             PrintStream medianUniqueWordsOutput) {
      this.runningMedian = runningMedian;
      this.medianUniqueWordsOutput = medianUniqueWordsOutput;
    }

    /**
     * Receives counts from sequenced messages and buffers those from an unexpected sequence number
     * while outputting the running median for each expected sequence number.
     */
    @Override
    public void run() {
      HashMap<Integer, Integer> buffer = new HashMap<>();
      int expectedSequence = 0;
      boolean keepRunning = true;
      do {
        try {
          SequencedCount sc = queue.remove();
          buffer.put(sc.sequence(), sc.count());
          while (buffer.containsKey(expectedSequence)) {
            stepRunningMedianWith(buffer.remove(expectedSequence));
            expectedSequence++;
          }
        } catch (NoSuchElementException nse) {
          // If the queue is empty we wait a 20th of a second to see if the pool is terminating.
          try {
            keepRunning = !pool.awaitTermination(50L, TimeUnit.MILLISECONDS);
          } catch (InterruptedException ie) {
            // If not, we'll keep running looking for more in the queue.
          }
        }
      } while (keepRunning);
      medianUniqueWordsOutput.close();
    }

    private void stepRunningMedianWith(Integer uniqueWords) {
      medianUniqueWordsOutput.printf("%.2f\n", runningMedian.update(uniqueWords));
    }
  }
}
