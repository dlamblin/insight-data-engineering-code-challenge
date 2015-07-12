package lamblin.wordcount;

import java.io.PrintStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.ObjectGraph;
import lamblin.common.Module;
import lamblin.common.source.line.LineSource;

/**
 * The Insight Data Engineering Coding Challenge issued 2015-07-02
 * The Java version.
 *
 * First part, counting words.
 *
 * @author Daniel Lamblin
 */
public class WordCountCmd {

  @Inject
  MessageWordCounter messageWordCounter;

  @Inject
  @Named("input")
  LineSource source;

  @Inject
  PrintStream output;

  @Inject
  ConcurrentLinkedQueue<SequencedCount> medianQueue;

  public static void main(String[] args) {
    // Setup injection based on arguments
    ObjectGraph objectGraph = ObjectGraph.create(new WordCountModule(), new Module(args));
    WordCountCmd wordCountCmd = objectGraph.get(WordCountCmd.class);

    // Start counting words
    wordCountCmd.countWords();
  }

  private void countWords() {
    int sequence = 0;
    for (String line : source) {
      messageWordCounter.addMessage(sequence++, line);
    }
    for (String word : messageWordCounter.getSortedWords()) {
      int count = messageWordCounter.getCount(word);
      output.printf("%-27s\t%d\n", word, count);
    }
    output.close();
  }
}
