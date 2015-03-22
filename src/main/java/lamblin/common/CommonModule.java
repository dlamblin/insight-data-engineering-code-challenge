package lamblin.common;

import com.beust.jcommander.JCommander;

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Provides;
import lamblin.source.word.DirectoryWordSource;
import lamblin.source.word.EmptyWordSource;
import lamblin.source.word.FileWordSource;
import lamblin.source.word.InputStreamWordSource;
import lamblin.source.word.WordSource;
import lamblin.wordcount.WordCleaner;

/**
 * Created by dlamblin on 3/22/15.
 *
 * @author Daniel Lamblin
 */
@dagger.Module(
    library = true
)
public class CommonModule {

  private final String[] args;

  /**
   * {@link CommonModule} configures parts of both {@link lamblin.wordcount.WordCountCmd}
   * and {@link lamblin.medianwordsperline.RunningMedianWordsPerLineCmd}.
   *
   * @param args command line arguments to parse
   */
  public CommonModule(String[] args) {
    this.args = args;
  }

  /**
   * Identifies the type of file or directory (or none) given as an arguments and provides the
   * appropriate {@link lamblin.source.word.WordSource} for it.
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
  public static WordSource getWordSourceForArgument(String argName, String arg, boolean useStdIn) {
    if (arg == null) {
      return useStdIn ? new InputStreamWordSource() : new EmptyWordSource();
    } else {
      File file = new File(arg);
      if (file.isDirectory()) {
        return new DirectoryWordSource(file);
      } else if (file.isFile()) {
        return new FileWordSource(file);
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
  @Named("stop words")
  /**
   * Provides the {@link WordSource} for the stop-words argument.
   */
  WordSource provideStopWordSource(Arguments arguments) {
    final String argName = "--stopwords";
    final String arg = arguments.stopwords;
    return CommonModule.getWordSourceForArgument(argName, arg, false);
  }

  @Provides
  @Singleton
  /**
   * Provides the {@link lamblin.wordcount.WordCleaner} which also uses the stop-words.
   */
  WordCleaner provideWordCleaner(@Named("stop words") WordSource stopWordSource) {
    return new WordCleaner(stopWordSource);
  }
}
