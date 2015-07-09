package lamblin.common;

import com.beust.jcommander.Parameter;

/**
 * Arguments to parse for {@link lamblin.wordcount.WordCountCmd}
 * or {@link lamblin.medianwordsperline.RunningMedianWordsPerLineCmd}.
 *
 * Created by dlamblin on 3/21/15.
 *
 * @author Daniel Lamblin
*/
public class Arguments {

  @Parameter(
      names = {"-h", "--help"},
      help = true)
  public boolean help;

  @Parameter(
      names = {"-i", "--input"},
      description = "Input file or directory path")
  public String input;

  @Parameter(
      names = {"-o", "--output"},
      description = "Output file path")
  public String output;

  @Parameter(
      names = {"-s", "--stopwords"},
      description = "Stop-words file path containing words to be ignored")
  public String stopwords;

  @Parameter(
      names = {"-u", "--unconstrained"},
      description = "Swaps out the range limited median for the MinMax queue median method")
  public boolean unconstrained;
}
