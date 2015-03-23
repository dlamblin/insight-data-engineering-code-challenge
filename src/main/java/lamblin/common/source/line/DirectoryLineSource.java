package lamblin.common.source.line;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import javax.sound.sampled.Line;

import lamblin.common.source.EmptySource;
import lamblin.common.source.word.FileWordSource;
import lamblin.common.source.word.WordSource;

/**
 * Reads lines out of each file in a directory. Does not recurse directories.
 *
 * TODO(lamblin): some kind of generified version between this and {@link @DirectoryWordSource}.
 * Created by dlamblin on 3/22/15.
 *
 * @author Daniel Lamblin
 */
public class DirectoryLineSource implements LineSource {

  private final File file;
  private final DirectoryStream<Path> directoryStream;

  public DirectoryLineSource(File file) {
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
    final Iterable<String> directoryLineSource = Iterables.concat(new FilesIterable());
    Iterator<String> lineIterator = directoryLineSource.iterator();
    if (!lineIterator.hasNext()) {
      System.err.println("Directory \"" + file.getName() + "\" contains no files.");
    }
    return lineIterator;
  }

  private class FilesIterable implements Iterable<LineSource> {

    @Override
    public Iterator<LineSource> iterator() {
      return Iterables.transform(
          directoryStream,
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
