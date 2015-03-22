package lamblin.source.word;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Reads words out of each file in a directory. Does not recurse directories.
 *
 * Created by dlamblin on 3/21/15.
 *
 * @author Daniel Lamblin
*/
public class DirectoryWordSource implements WordSource {

  private final File file;
  private final DirectoryStream<Path> directoryStream;

  public DirectoryWordSource(File file) {
    this.file = file;
    DirectoryStream<Path> dirStream;
    try {
      dirStream = Files.newDirectoryStream(file.toPath());
    } catch (IOException e) {
      System.err.println("Directory \"" + file.getName() + "\" could not be opened.");
      e.printStackTrace();
      dirStream = null;
    }
    directoryStream = dirStream;
  }

  @Override
  public Iterator<String> iterator() {
    final Iterable<String> directoryWordSource = Iterables.concat(new FilesIterable());
    Iterator<String> wordIterator = directoryWordSource.iterator();
    if (!wordIterator.hasNext()) {
      System.err.println("Directory \"" + file.getName() + "\" contains no files.");
    }
    return wordIterator;
  }

  private class FilesIterable implements Iterable<WordSource> {

    @Override
    public Iterator<WordSource> iterator() {
      return Iterables.transform(
          directoryStream,
          new Function<Path, WordSource>() {
            @Override
            public WordSource apply(Path input) {
              final File file = input.toFile();
              return file.isFile() ? new FileWordSource(file) : new EmptyWordSource();
            }
          }
      ).iterator();
    }
  }
}
