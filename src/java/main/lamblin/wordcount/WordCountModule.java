package lamblin.wordcount;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Provides;
import lamblin.common.Arguments;
import lamblin.common.Module;
import lamblin.common.source.word.WordSource;

/**
 * Configures the state of {@link WordCountCmd} and its dependencies,
 * using {@link lamblin.common.Module}'s {@link lamblin.common.Arguments}.
 *
 * Created by dlamblin on 3/21/15.
 *
 * @author Daniel Lamblin
 */
@dagger.Module(
    includes = { Module.class },
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
    return Module.getWordSourceForArgument(argName, arg, true);
  }

  @Provides
  @Singleton
  /**
   * Provides the {@link WordAccumulator} which counts the words found.
   */
  WordAccumulator provideWordAccumulator() {
    return new WordAccumulator();
  }
}
