package lamblin.wordcount;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import com.beust.jcommander.internal.Sets;

import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Accumulates counts of each word from messages added and total words added.
 * Also retains a sorted set of words.
 *
 * @author Daniel Lamblin
 */
public class MessageWordCounter {

  private static final TimeUnit poolTimeoutUnits = TimeUnit.SECONDS;
  private static final long poolTimeout = 5;
  private static final Splitter splitter = Splitter.on(CharMatcher.BREAKING_WHITESPACE)
      .trimResults().omitEmptyStrings();
  private final ExecutorService pool;
  private final Multiset<String> words = ConcurrentHashMultiset.create();
  private final ConcurrentSkipListSet<String> sortedWords = new ConcurrentSkipListSet<>();
  private final ConcurrentLinkedQueue<SequencedCount> queue;

  public MessageWordCounter(ConcurrentLinkedQueue<SequencedCount> queue) {
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
    pool.execute(new Counter(sequence, message));
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

  /**
   * Adds a message's words to the counting and sorted sets.
   */
  private class Counter implements Runnable {

    private final int sequence;
    private final String message;

    public Counter(int sequence, String message) {
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
        queue.add(SequencedCount.create(sequence, uniques.size()));
      }
    }
  }
}
