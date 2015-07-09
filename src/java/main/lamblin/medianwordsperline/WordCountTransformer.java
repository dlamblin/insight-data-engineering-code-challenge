package lamblin.medianwordsperline;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.io.StringReader;

import lamblin.common.source.word.ReaderWordSource;
import lamblin.common.source.word.filter.WordCleaner;

/**
 * Cleans the words on the lines of non-AlphaNumerics, and removes empty or all-digit words, then
 * it counts the remaining words on that line. Limited to {@link Integer.MAX_VALUE}.
 *
 * Created by dlamblin on 3/22/15.
 *
 * @author Daniel Lamblin
 */
public class WordCountTransformer implements Function<String, Integer> {

  private static final String string = "String";
  private final WordCleaner wordCleaner;

  public WordCountTransformer(WordCleaner wordCleaner) {
    this.wordCleaner = wordCleaner;
  }

  @Override
  public Integer apply(String input) {
    return Iterables.size(wordCleaner.all(new ReaderWordSource(new StringReader(input), string)));
  }
}
