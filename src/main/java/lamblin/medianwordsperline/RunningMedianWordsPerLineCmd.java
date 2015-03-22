package lamblin.medianwordsperline;

import java.io.PrintStream;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.ObjectGraph;
import lamblin.common.CommonModule;
import lamblin.source.word.WordSource;
import lamblin.wordcount.WordCleaner;

/**
 * The Insight Data Engineering Coding Challenge issued 2015-03-17
 * The Java version.
 *
 * Second part, running median of words per line.
 *
 * Created by dlamblin on 3/22/15.
 *
 * @author Daniel Lamblin
 */
public class RunningMedianWordsPerLineCmd {

  @Inject
  WordCleaner wordCleaner;

  @Inject
  @Named("input")
  WordSource wordSource;

  @Inject
  PrintStream output;

  public static void main(String[] args) {
    // Setup injection based on arguments
    ObjectGraph objectGraph = ObjectGraph.create(new RunningMedianModule(), new CommonModule(args));
    RunningMedianWordsPerLineCmd perLineCmd = objectGraph.get(RunningMedianWordsPerLineCmd.class);

    // Start counting words
    perLineCmd.countWordsPerLine();
  }

  private void countWordsPerLine() {
    Iterable<String> cleanWords = wordCleaner.all(wordSource);
    for (String word : cleanWords) {
    }
//    for (String word : wordAccumulator.getSortedWords()) {
//      int count = wordAccumulator.getCount(word);
//      output.printf("%-15s\t%d\n", word, count);
//    }
    output.close();
  }
}
