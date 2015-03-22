package lamblin.wordcount;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Provides;
import lamblin.common.Arguments;
import lamblin.common.CommonModule;
import lamblin.source.word.WordSource;

/**
 * Configures the state of {@link WordCountCmd} and its dependencies,
 * using {@link lamblin.common.CommonModule}'s {@link lamblin.common.Arguments}.
 *
 * Created by dlamblin on 3/21/15.
 *
 * @author Daniel Lamblin
 */
@dagger.Module(
    includes = { CommonModule.class },
    injects = WordCountCmd.class
)
class WordCountModule {

  @Provides
  @Singleton
  @Named("input")
  /**
   * Provides the {@link WordSource} for the input argument.
   */
  WordSource provideWordSource(Arguments arguments) {
    final String argName = "--input";
    final String arg = arguments.input;
    return CommonModule.getWordSourceForArgument(argName, arg, true);
  }

  @Provides
  @Singleton
  /**
   * Provides the {@link WordAccumulator} which counts the words found.
   */
  WordAccumulator provideWordAccumulator() {
    return new WordAccumulator();
  }

  @Provides
  @Singleton
  /**
   * Provides the {@link PrintStream} to write to.
   */
  PrintStream providePrintStream(Arguments arguments) {
    if (null != arguments.output) {
      File file = new File(arguments.output);
      try {
        return new PrintStream(file, StandardCharsets.UTF_8.name());
      } catch (FileNotFoundException | UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      System.err.println("Unable to open file \"" + arguments.output + "\" using stdout.");
    }
    return System.out;
  }
}
