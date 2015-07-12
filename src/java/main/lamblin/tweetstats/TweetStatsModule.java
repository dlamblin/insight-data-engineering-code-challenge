package lamblin.tweetstats;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.beust.jcommander.JCommander;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Provides;
import lamblin.common.source.InputStreamLineSource;
import lamblin.common.source.LineSource;
import lamblin.common.source.NoneToManyLineSource;
import lamblin.common.runningmedian.QueueRunningMedian;
import lamblin.common.runningmedian.RangeRunningMedian;
import lamblin.common.runningmedian.RunningMedian;

/**
 * Configures the state of {@link TweetStatsCmd} and its dependencies,
 * using {@link Arguments}.
 *
 * @author Daniel Lamblin
 */
@dagger.Module(
    injects = TweetStatsCmd.class
)
class TweetStatsModule {

  private final String[] args;

  /**
   * {@link TweetStatsModule} configures parts of {@link TweetStatsCmd}.
   *
   * @param args command line arguments to parse
   */
  public TweetStatsModule(String[] args) {
    this.args = args;
  }

  @Provides
  @Singleton
  Arguments provideArguments() {
    Arguments arguments = new Arguments();
    JCommander cmd = new JCommander(arguments, args);
    if (arguments.help) {
      cmd.usage();
      System.exit(0);
    }
    return arguments;
  }

  @Provides
  @Singleton
  @Named("ft1.txt")
  /**
   * Provides the {@link java.io.PrintStream} to write to.
   */
  PrintStream providePrintStreamFt1(Arguments arguments) {
    return providePrintStream(arguments.output, "ft1.txt");
  }

  @Provides
  @Singleton
  @Named("ft2.txt")
  /**
   * Provides the {@link java.io.PrintStream} to write to.
   */
  PrintStream providePrintStreamFt2(Arguments arguments) {
    return providePrintStream(arguments.output, "ft2.txt");
  }

  private PrintStream providePrintStream(String dirArgument, String filename) {
    if (null != dirArgument) {
      File file = new File(dirArgument);
      if (file.isDirectory()) {
        file = Paths.get(dirArgument, filename).toFile();
        try {
          return new PrintStream(file, StandardCharsets.UTF_8.name());
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
          e.printStackTrace();
        }
        System.err.println("Unable to open file \"" + file.getPath() + "\" using stdout.");
      } else {
        System.err.println("The output directory \"" + dirArgument + "\" isn't a directory.");
      }
    }
    return System.out;
  }

  /**
   * Provides the {@link LineSource} for the input argument.
   *
   * Identifies the type of file or directory (or none) given as an argument and provides the
   * appropriate {@link LineSource} for it. Opens {@code stdin} if {@code arg} is null.
   *
   * Note that Dagger does not allow for any provider and thus neither any constructor used in this
   * provider to throw an exception. For that reason errors are output to stderr, and most of the
   * constructors for these word sources try to catch exceptions.
   */
  @Provides
  @Singleton
  @Named("input")
  LineSource provideLineSource(Arguments arguments) {
    ArrayList<String> inputs = Lists.newArrayList(
        Iterables.concat(arguments.inputs, arguments.remainingInputs));
    if (inputs.isEmpty()) {
      return new InputStreamLineSource();
    } else {
      // Check if all -i and remaining inputs are valid files or directories
      for (String input : inputs) {
        File file = new File(input);
        if (!file.isDirectory() && !file.isFile()) {
          System.err.println(
              "Error: argument specified was not a file or directory: " + input);
          System.exit(-1);
        }
      }
      return new NoneToManyLineSource(inputs);
    }
  }


  @Provides
  /**
   * Provides a {@link RunningMedian} of unique word counts of each added line.
   */
  RunningMedian<Integer> provideQueueRunningMedian(Arguments arguments) {
    return arguments.unconstrained ? new QueueRunningMedian<Integer>()
                                   : new RangeRunningMedian<Integer>(0L, 70L, 1L);
  }

  @Provides
  @Singleton
  /**
   * Provides the {@link MessageWorkerPool} which counts the words found.
   */
  MessageWorkerPool provideMessageWordCounter(ConcurrentLinkedQueue<SequencedCount> queue) {
    return new MessageWorkerPool(queue);
  }

  @Provides
  @Singleton
  /**
   * Provides a {@link concurrentLinkedQueue} which passes {@link SequencedCount}s.
   */
  ConcurrentLinkedQueue<SequencedCount> provideConcurrentLinkedQueue() {
    return new ConcurrentLinkedQueue<>();
  }
}
