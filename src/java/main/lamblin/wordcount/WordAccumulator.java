package lamblin.wordcount;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Accumulates counts of each word added and total words added. Also retains a sorted set of words.
 *
 * Created by dlamblin on 3/21/15.
 *
 * @author Daniel Lamblin
 */
public class WordAccumulator {

  private static final TimeUnit poolTimeoutUnits = TimeUnit.SECONDS;
  private static final long poolTimeout = 5;
  private final ExecutorService pool;
  private final Multiset<String> words = ConcurrentHashMultiset.create();
  private final ConcurrentSkipListSet<String> sortedWords = new ConcurrentSkipListSet<>();

  public WordAccumulator() {
    pool = Executors.newWorkStealingPool();
  }

  /**
   * Adds words to be counted. You cannot add words after calling either {@link #getCounts} or
   * {@link #getSortedWords}.
   *
   * @param word the word for which to increment its count
   */
  public void add(String word) {
    pool.execute(new Counter(word));
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
   * thread pool and disable the {@link #add} method.
   *
   * @return a set of words and their counts
   */
  public Set<Multiset.Entry<String>> getCounts() {
    terminatePool();
    return words.entrySet();
  }

  /**
   * The ordered set of words counted. It does not include their counts. Getting this will terminal
   * the accumulator's thread pool and disable the {@link #add} method.
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
   * Adds a word to the counting and sorted sets.
   */
  private class Counter implements Runnable {

    private final String word;

    public Counter(String word) {
      this.word = word;
    }

    @Override
    public void run() {
      words.add(word);
      sortedWords.add(word);
    }
  }
}
