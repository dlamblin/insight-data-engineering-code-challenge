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
class TweetStatsCmd {

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

  public static void main(String[] args) {
    // Setup injection based on arguments
    ObjectGraph objectGraph = ObjectGraph.create(new TweetStatsModule(args));
    TweetStatsCmd tweetStatsCmd = objectGraph.get(TweetStatsCmd.class);

    tweetStatsCmd.startRunningMedian();
    // Start counting words
    tweetStatsCmd.countWords();
    tweetStatsCmd.medianUniqueWordsOutput.close();
  }

  private void countWords() {
    int sequence = 0;
    for (String line : source) {
      messageWorkerPool.addMessage(sequence++, line);
    }
    for (String word : messageWorkerPool.getSortedWords()) {
      int count = messageWorkerPool.getCount(word);
      wordCountOutput.printf("%-27s %d\n", word, count);
    }
    wordCountOutput.close();
  }

  private void startRunningMedian() {
    messageWorkerPool.setupMedianUniqueWords(runningMedian, medianUniqueWordsOutput);
  }
}
