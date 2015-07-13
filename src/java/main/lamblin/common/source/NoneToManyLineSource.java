package lamblin.common.source;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.io.File;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * This implementation of a line source will provide lines from multiple specified files
 * or directories. With none specified it will provide lines from the input stream.
 *
 * @author Daniel Lamblin
 */
public class NoneToManyLineSource implements LineSource {

  private final Iterable<String> filesOrDirs;

  /**
   * Takes a iterable collection of strings assumed to point to named files or directories and
   * provides an iterator over each of the lines within those files and the directories' files.
   * If the iterable of strings is empty then the iterator will iterate over the lines in the input
   * stream.
   *
   * @param filesOrDirs a collection of names of files or directories or an empty collection
   */
  public NoneToManyLineSource(Iterable<String> filesOrDirs) {
    this.filesOrDirs = filesOrDirs;
  }

  /**
   * @return an iterator over the lines in each of the files specified or on directories specified.
   *         If none are specified it opens stdin.
   */
  @Override
  public Iterator<String> iterator() {
    if (filesOrDirs != null && filesOrDirs.iterator().hasNext()) {
      return Iterables.concat(new FilesOrDirsLines()).iterator();
    } else {
      return new InputStreamLineSource().iterator();
    }
  }

  /**
   * Convert each string in the {@code filesOrDirs} into a line source that is one of:
   * <ul>
   * <li>A {@link FileLineSource} if the string points to a file.</li>
   * <li>A {@link DirectoryLineSource} if the string points to a directory.</li>
   * <li>An {@link EmptySource} if the string does not point to one of these.</li>
   * </ul>
   */
  private class FilesOrDirsLines implements Iterable<LineSource> {

    @Override
    public Iterator<LineSource> iterator() {
      return Iterables.transform(
          filesOrDirs,
          input -> {
            final File file = Paths.get(input).toFile();
            return file.isFile() ? new FileLineSource(file)
                                 : file.isDirectory() ? new DirectoryLineSource(file)
                                                      : new EmptySource();
          }
      ).iterator();
    }
  }
}
