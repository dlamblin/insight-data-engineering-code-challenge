package lamblin.tweetstats;

import java.io.PrintStream;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.ObjectGraph;
import lamblin.common.source.LineSource;
import lamblin.common.runningmedian.RunningMedian;

/**
 * The Insight Data Engineering Coding Challenge issued 2015-07-02
 * The Java version.
 *
 * First part: counting words also sends unique word count per message to
 * Second part: the running median of unique words
 *
 * @author Daniel Lamblin
 */
public class TweetStatsCmd {

  @Inject
  MessageWorkerPool messageWorkerPool;

  @Inject
  @Named("input")
  LineSource source;

  @Inject
  @Named("ft1.txt")
  PrintStream wordCountOutput;

  @Inject
  @Named("ft2.txt")
  PrintStream medianUniqueWordsOutput;

  @Inject
  RunningMedian<Integer> runningMedian;

  /**
   * Sets up the Dagger injection module using the arguments which are parsed by JCommander.
   * It then starts the worker pool, starts the runningMedian task, send messages to the pool, and
   * outputs the total counts appropriately. Then it closes the two output print streams.
   *
   * @param args command line arguments to be parsed by {@link Arguments}
   */
  public static void main(String[] args) {
    // Setup injection based on arguments
    ObjectGraph objectGraph = ObjectGraph.create(new TweetStatsModule(args));
    TweetStatsCmd tweetStatsCmd = objectGraph.get(TweetStatsCmd.class);

    tweetStatsCmd.startRunningMedian();
    // Start counting words
    tweetStatsCmd.countWords();
    tweetStatsCmd.medianUniqueWordsOutput.close();
  }

  /**
   * Sends each line from the sources specified on the command line into the
   * {@link MessageWorkerPool#addMessage(String)}. Then it outputs all the words and their counts in
   * sorted order, closing the output {@link PrintStream}.
   */
  private void countWords() {
    for (String line : source) {
      messageWorkerPool.addMessage(line);
    }
    for (String word : messageWorkerPool.getSortedWords()) {
      int count = messageWorkerPool.getCount(word);
      wordCountOutput.printf("%-27s %d\n", word, count);
    }
    wordCountOutput.close();
  }

  private void startRunningMedian() {
    messageWorkerPool.startUniqueWordsRunningMedian(runningMedian, medianUniqueWordsOutput);
  }
}
