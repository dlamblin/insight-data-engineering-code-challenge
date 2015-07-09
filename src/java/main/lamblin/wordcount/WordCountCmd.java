package lamblin.wordcount;

import java.io.PrintStream;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.ObjectGraph;
import lamblin.common.Module;
import lamblin.common.source.word.filter.WordCleaner;
import lamblin.common.source.word.WordSource;

/**
 * The Insight Data Engineering Coding Challenge issued 2015-03-17
 * The Java version.
 *
 * First part, counting words.
 *
 * Created by dlamblin on 3/21/15.
 *
 * @author Daniel Lamblin
 */
public class WordCountCmd {

  @Inject
  WordAccumulator wordAccumulator;

  @Inject
  WordCleaner wordCleaner;

  @Inject
  @Named("input")
  WordSource wordSource;

  @Inject
  PrintStream output;

  public static void main(String[] args) {
    // Setup injection based on arguments
    ObjectGraph objectGraph = ObjectGraph.create(new WordCountModule(), new Module(args));
    WordCountCmd wordCountCmd = objectGraph.get(WordCountCmd.class);

    // Start counting words
    wordCountCmd.countWords();
  }

  private void countWords() {
    Iterable<String> cleanWords = wordCleaner.all(wordSource);
    for (String word : cleanWords) {
      wordAccumulator.add(word);
    }
    for (String word : wordAccumulator.getSortedWords()) {
      int count = wordAccumulator.getCount(word);
      output.printf("%-15s\t%d\n", word, count);
    }
    output.close();
  }
}
