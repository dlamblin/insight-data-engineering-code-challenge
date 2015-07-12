package lamblin.common.source.line;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import lamblin.common.source.EmptySource;

/**
 * Reads lines out of each file in a directory. Does not recurse directories.
 *
 * @author Daniel Lamblin
 */
public class DirectoryLineSource implements LineSource {

  private final Iterable<Path> paths;

  public DirectoryLineSource(File file) {
    Iterable<Path> pathStream;
    try {
      pathStream = Files.newDirectoryStream(file.toPath());
    } catch (IOException e) {
      System.err.println("Directory \"" + file.getName() + "\" could not be opened.");
      e.printStackTrace();
      // To avoid giving the Iterables.transform a null fromIterable we use a Path which is itself
      // an Iterable<Path>. Importantly "." is a directory and never a file at line 54.
      pathStream = Paths.get(".");
    }
    paths = pathStream;
  }

  @Override
  public Iterator<String> iterator() {
    return Iterables.concat(new FilesIterable()).iterator();
  }

  private class FilesIterable implements Iterable<LineSource> {

    @Override
    public Iterator<LineSource> iterator() {
      return Iterables.transform(
          paths,
          new Function<Path, LineSource>() {
            @Override
            public LineSource apply(Path input) {
              final File file = input.toFile();
              return file.isFile() ? new FileLineSource(file) : new EmptySource();
            }
          }
      ).iterator();
    }
  }
}
