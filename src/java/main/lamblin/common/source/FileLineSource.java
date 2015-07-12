package lamblin.common.source;

import java.io.CharArrayReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 * Reads lines out of a {@link File}.
 *
 * @author Daniel Lamblin
 */
public class FileLineSource extends ReaderLineSource {

  /**
   * Opens a file reader or if the file is invalid, an empty char array as a line source.
   *
   * @param file the file to be opened
   */
  public FileLineSource(File file) {
    super(getFileReader(file), "File: \"" + file.getName() + "\"");
  }

  /**
   * Opens a file reader or an empty char array as a line source.
   * Because Dagger really doesn't want any thrown errors in constructors or providers,
   * this is the easiest way to gracefully read no words from an non-existent file.
   *
   * @param file the file to be opened
   * @return either a file reader for that file or a empty array reader if the file wasn't found
   */
  private static Reader getFileReader(File file) {
    try {
      return new FileReader(file);
    } catch (FileNotFoundException e) {
      System.err.println("Unable to open file: \"" + file.getName() + "\"");
      e.printStackTrace();
      return new CharArrayReader(new char[]{});
    }
  }
}
