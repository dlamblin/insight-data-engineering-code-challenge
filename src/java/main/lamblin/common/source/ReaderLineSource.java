package lamblin.common.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Reads words out of input {@link java.io.Reader}.
 *
 * Created by dlamblin on 3/22/15.
 *
 * @author Daniel Lamblin
 */
class ReaderLineSource implements LineSource {
  private final BufferedReader input;
  private final String sourceDescription;

  ReaderLineSource(Reader in, String description) {
    sourceDescription = description;
    input = (in instanceof BufferedReader) ? (BufferedReader) in : new BufferedReader(in);
  }

  @Override
  public Iterator<String> iterator() {
    return new iterator();
  }

  class iterator implements Iterator<String> {

    private String nextLine;
    private boolean closed = false;

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

    private void close() throws IOException {
      if (!closed) {
        input.close();
        closed = true;
      }
    }
  }
}
