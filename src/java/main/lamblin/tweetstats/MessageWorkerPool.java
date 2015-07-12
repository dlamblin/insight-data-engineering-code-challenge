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
 * Accumulates counts of each word from messages added and total words added.
 * Also retains a sorted set of words.
 *
 * @author Daniel Lamblin
 */
class MessageWorkerPool {

  private static final TimeUnit poolTimeoutUnits = TimeUnit.SECONDS;
  private static final long poolTimeout = 5;
  private static final Splitter splitter = Splitter.on(CharMatcher.BREAKING_WHITESPACE)
      .trimResults().omitEmptyStrings();
  private final ExecutorService pool;
  private final Multiset<String> words = ConcurrentHashMultiset.create();
  private final ConcurrentSkipListSet<String> sortedWords = new ConcurrentSkipListSet<>();
  private final ConcurrentLinkedQueue<SequencedCount> queue;

  public MessageWorkerPool(ConcurrentLinkedQueue<SequencedCount> queue) {
    this.queue = queue;
    pool = Executors.newWorkStealingPool();
  }

  /**
   * Adds messages with words to be counted. You cannot add messages after calling either
   * {@link #getCounts} or {@link #getSortedWords}.
   *
   * @param sequence the sequence of this message for the running median of unique words counted
   * @param message the message for which to increment its words' counts
   */
  public void addMessage(int sequence, String message) {
    pool.execute(new WordCounter(sequence, message));
  }

  /**
   * The total number of words added.
   *
   * @return the total number of all words
   */
  public int getSize() {
    return words.size();
  }

  /**
   * How many of these words were counted.
   *
   * @param word the word counted
   * @return the number of times it was added
   */
  public int getCount(String word) {
    return words.count(word);
  }

  /**
   * The unordered entry set of words and their counts. Getting this will terminal the accumulator's
   * thread pool and disable the {@link #addMessage} method.
   *
   * @return a set of words and their counts
   */
  public Set<Multiset.Entry<String>> getCounts() {
    terminatePool();
    return words.entrySet();
  }

  /**
   * The ordered set of words counted. It does not include their counts. Getting this will terminate
   * the accumulator's thread pool and disable the {@link #addMessage} method.
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

  public void setupMedianUniqueWords(RunningMedian<Integer> runningMedian,
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

    public WordCounter(int sequence, String message) {
      this.sequence = sequence;
      this.message = message;
    }

    @Override
    public void run() {
      Set<String> uniques = Sets.newHashSet();
      for (String word : splitter.split(message)) {
        uniques.add(word);
        words.add(word);
        sortedWords.add(word);
      }
      queue.add(SequencedCount.create(sequence, uniques.size()));
    }
  }

  /**
   * Dequeue {@link SequencedCount} objects and output the running median of the counts in sequence.
   */
  private class RunningMedianTask implements Runnable {

    private final RunningMedian<Integer> runningMedian;
    private final PrintStream medianUniqueWordsOutput;

    public RunningMedianTask(RunningMedian<Integer> runningMedian,
                             PrintStream medianUniqueWordsOutput) {
      this.runningMedian = runningMedian;
      this.medianUniqueWordsOutput = medianUniqueWordsOutput;
    }

    @Override
    public void run() {
      HashMap<Integer, Integer> buffer = new HashMap<>();
      Integer expectedSequence = 0;
      boolean keepRunning = true;
      do {
        try {
          SequencedCount sc = queue.remove();
          buffer.put(sc.sequence(), sc.count());
          while (buffer.containsKey(expectedSequence)) {
            stepRunningMedianWith(buffer.get(expectedSequence));
            expectedSequence++;
          }
        } catch (NoSuchElementException nse) {
          try {
            keepRunning = !pool.awaitTermination(500L, TimeUnit.MILLISECONDS);
          } catch (InterruptedException ie) {
            // keep running
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
