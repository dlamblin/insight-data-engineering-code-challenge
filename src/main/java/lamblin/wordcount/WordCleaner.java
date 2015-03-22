package lamblin.wordcount;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.regex.Pattern;

import lamblin.source.word.WordSource;

/**
 * This class cleans up the each iterated word by removing non-AlphaNumerics, stop words, and words
 * which are entirely digits.
 *
 * Created by dlamblin on 3/21/15.
 *
 * @author Daniel Lamblin
 */
public class WordCleaner {

  private static Pattern nonAlphaNumerics = Pattern.compile("\\W");
  private static Pattern entirelyNumbers = Pattern.compile("^\\d*$");

  private final Function<String, String> cleanWordFunction = new Function<String, String>() {
    @Override
    /**
     * LowerCases input, then removes any non-AlphaNumeric characters,
     * then empties any remaining string that is entirely digits (a number).
     */
    public String apply(String input) {
      input = input.toLowerCase();
      input = nonAlphaNumerics.matcher(input).replaceAll("");
      return entirelyNumbers.matcher(input).replaceAll("");
    }
  };

  private WordSource stopWordsSource;
  private HashSet<String> stopWordSet = Sets.newHashSet();

  /**
   * Skip empty words or stop words.
   */
  private final Predicate<String> nonStopWordPredicate = new Predicate<String>() {
    @Override
    public boolean apply(String input) {
      return !input.isEmpty() && !stopWordSet.contains(input);
    }
  };

  public WordCleaner(WordSource stopWordsSource) {
    this.stopWordsSource = stopWordsSource;
    initializeStopWords();
  }

  /**
   * Cleans the input as a word without non-AlphaNumeric characters, and empties strings which are
   * numbers.
   *
   * @param input the word to clean
   * @return the cleaned result
   */
  public String word(String input) {
    return cleanWordFunction.apply(input);
  }

  /**
   * Cleans all words as they're iterated and skips over words that were stop-words or were emptied
   * by cleaning.
   *
   * @param source a source of words
   * @return an {@link Iterable} lazily cleaned up version of the source {@link Iterable}
   */
  public Iterable<String> all(Iterable<String> source) {
    //Clean the words from the input source.
    Iterable<String> cleanedWords = Iterables.transform(source, cleanWordFunction);
    //Filter out empty strings and stop words.
    return Iterables.filter(cleanedWords, nonStopWordPredicate);
  }

  private void initializeStopWords() {
    for (String stopWord : stopWordsSource) {
      stopWord = word(stopWord);
      if (!stopWord.isEmpty()) {
        stopWordSet.add(stopWord);
      }
    }
  }
}
