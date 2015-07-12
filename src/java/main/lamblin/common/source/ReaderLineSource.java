package lamblin.common.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Reads lines out of the input {@link java.io.Reader}. This is used in the {@link FileLineSource}
 * and {@link InputStreamLineSource} after opening their respective {@link Reader}.
 *
 * @author Daniel Lamblin
 */
public class ReaderLineSource implements LineSource {
  private final BufferedReader input;
  private final String sourceDescription;

  /**
   * Sets up a line iterator over the input {@link Reader}.
   *
   * @param in to read lines from when iterating
   * @param description to describe the input for subclasses
   */
  public ReaderLineSource(Reader in, String description) {
    sourceDescription = description;
    input = (in instanceof BufferedReader) ? (BufferedReader) in : new BufferedReader(in);
  }

  /**
   * @return an iterator over the lines in the {@link Reader} input provided
   */
  @Override
  public Iterator<String> iterator() {
    return new iterator();
  }

  class iterator implements Iterator<String> {

    private String nextLine;
    private boolean closed = false;

    /**
     * @return {@code true} if the input has another line available, {@code false} otherwise
     */
    @Override
    public boolean hasNext() {
      if (closed) {
        return false;
      }
      while (null == nextLine) {
        try {
          nextLine = input.readLine();
          if (null == nextLine) {
            close();
            return false;
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      return true;
    }

    /**
     * @return the next line from the input being read
     */
    @Override
    public String next() {
      if (hasNext()) {
        String result = nextLine;
        nextLine = null;
        return result;
      } else {
        throw new NoSuchElementException(
            sourceDescription + " has ended with no more words available.");
      }
    }

    /**
     * Closes the underlying input stream. Need only be called once.
     *
     * @throws IOException if the input stream cannot be closed.
     */
    private void close() throws IOException {
      if (!closed) {
        input.close();
        closed = true;
      }
    }
  }
}
