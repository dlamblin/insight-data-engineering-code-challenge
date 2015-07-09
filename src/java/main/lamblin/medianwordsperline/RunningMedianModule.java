package lamblin.medianwordsperline;

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Provides;
import lamblin.common.Arguments;
import lamblin.common.Module;
import lamblin.common.source.EmptySource;
import lamblin.common.source.line.DirectoryLineSource;
import lamblin.common.source.line.FileLineSource;
import lamblin.common.source.line.InputStreamLineSource;
import lamblin.common.source.line.LineSource;
import lamblin.common.source.word.filter.WordCleaner;

/**
 * Configures the state of {@link RunningMedianWordsPerLineCmd} and its dependencies,
 * using {@link lamblin.common.Module}'s {@link lamblin.common.Arguments}.
 *
 * TODO(lamblin): remove duplication between the {@link lamblin.wordcount.WordCountModule} and this.
 * Created by dlamblin on 3/22/15.
 *
 * @author Daniel Lamblin
 */
@dagger.Module(
    includes = {Module.class},
    injects = RunningMedianWordsPerLineCmd.class
)
public class RunningMedianModule {

  /**
   * Identifies the type of file or directory (or none) given as an argument and provides the
   * appropriate {@link lamblin.common.source.line.LineSource} for it.
   * Optionally opens {@code stdin} if {@code arg} is null.
   *
   * Note that Dagger does not allow for any provider and thus neither any constructor used in this
   * provider to throw an exception. For that reason errors are output to stderr, and most of the
   * constructors for these word sources try to catch exceptions.
   *
   * @param argName  the name of the argument specifying the source
   * @param arg      the string path provided as an argument for the source
   * @param useStdIn if arg is null {@code true} if should we read stdin
   */
  public static LineSource getLineSourceForArgument(String argName, String arg, boolean useStdIn) {
    if (arg == null) {
      return useStdIn ? new InputStreamLineSource() : new EmptySource();
    } else {
      File file = new File(arg);
      if (file.isDirectory()) {
        return new DirectoryLineSource(file);
      } else if (file.isFile()) {
        return new FileLineSource(file);
      } else {
        System.err.println(
            "Error: " + argName + " argument specified was not a file or directory: " + arg);
        System.exit(-1);
      }
    }
    return null;
  }

  @Provides
  @Singleton
  @Named("input")
  /**
   * Provides the {@link LineSource} for the input argument.
   */
  LineSource provideLineSource(Arguments arguments) {
    final String argName = "--input";
    final String arg = arguments.input;
    return getLineSourceForArgument(argName, arg, true);
  }

  @Provides
  @Singleton
  WordCountTransformer provideWordCountTransformer(WordCleaner wordCleaner) {
    return new WordCountTransformer(wordCleaner);
  }

  @Provides
  /**
   * Provides a {@link RunningMedian} of word counts of each added line.
   */
  RunningMedian<String> provideQueueRunningMedian(Arguments arguments,
                                                  WordCountTransformer wordCountTransformer) {
    return new TransformingRunningMedian<>(
        wordCountTransformer, (arguments.unconstrained)
                              ? new QueueRunningMedian<Integer>()
                              : new RangeRunningMedian<Integer>(0L, 50L, 1L));
  }
}
