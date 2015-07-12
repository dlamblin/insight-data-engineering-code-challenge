package lamblin.wordcount;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Provides;
import lamblin.common.Arguments;
import lamblin.common.Module;
import lamblin.common.source.line.DirectoryLineSource;
import lamblin.common.source.line.FileLineSource;
import lamblin.common.source.line.InputStreamLineSource;
import lamblin.common.source.line.LineSource;
import lamblin.common.source.line.NoneToManyLineSource;

/**
 * Configures the state of {@link WordCountCmd} and its dependencies,
 * using {@link lamblin.common.Module}'s {@link lamblin.common.Arguments}.
 *
 * @author Daniel Lamblin
 */
@dagger.Module(
    includes = { Module.class },
    injects = WordCountCmd.class
)
class WordCountModule {

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
  @Singleton
  /**
   * Provides the {@link MessageWordCounter} which counts the words found.
   */
  MessageWordCounter provideMessageWordCounter(ConcurrentLinkedQueue<SequencedCount> queue) {
    return new MessageWordCounter(queue);
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
