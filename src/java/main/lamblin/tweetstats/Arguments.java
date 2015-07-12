package lamblin.tweetstats;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Arguments to parse for {@link TweetStatsCmd}.
 *
 * @author Daniel Lamblin
*/
public class Arguments {

  /**
   * The help flag is either "-h" or "--help".
   */
  @Parameter(
      names = {"-h", "--help"},
      help = true)
  public boolean help;

  /**
   * The input flag is either "-i" or "--input" followed by an input file or directory path. This
   * may be specified multiple times and each instance is processed before arguments without a flag.
   */
  @Parameter(
      names = {"-i", "--input"},
      description = "Input file or directory path")
  public final List<String> inputs = new ArrayList<>();

  /**
   * The output flag is either "-o" or "--output" followed by a directory path on. This
   * may be specified once. The files "ft1.txt" and "ft2.txt" are [over] written in this directory.
   */
  @Parameter(
      names = {"-o", "--output"},
      description = "Output file path")
  public String output;

  /**
   * The "--unconstrained" flag, or "-u" for short is used to change the running median
   * implementation from the limited {@link lamblin.common.runningmedian.RangeRunningMedian} to the
   * less limited {@link lamblin.common.runningmedian.QueueRunningMedian}.
   */
  @Parameter(
      names = {"-u", "--unconstrained"},
      description = "Swaps out the range limited median for the MinMax queue median method")
  public boolean unconstrained;

  /**
   * Any remaining arguments not being a flag nor paired with one are gathered and processed as the
   * input flag would be, except after each of the input flags if any. If both input flags and these
   * remaining arguments are not specified, input is read from stdin.
   */
  @Parameter(description = "All remaining arguments are used as input files")
  public final List<String> remainingInputs = new ArrayList<>();
}
