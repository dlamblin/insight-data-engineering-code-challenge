package lamblin.source.word;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Reads words out of input {@link Reader}.
 *
 * TODO(lamblin): There's likely utility in pulling up the Splitter
 *
 * Created by dlamblin on 3/21/15.
 *
 * @author Daniel Lamblin
*/
public class ReaderWordSource implements WordSource {

  private final String sourceDescription;
  private final BufferedReader input;
  private final Splitter splitter;

  public ReaderWordSource(Reader in, String sourceDescription) {
    this.sourceDescription = sourceDescription;
    splitter = Splitter.on(CharMatcher.BREAKING_WHITESPACE)
        .trimResults().omitEmptyStrings();
    input = new BufferedReader(in);
  }

  @Override
  public Iterator<String> iterator() {
    return new iterator();
  }

  class iterator implements Iterator<String> {

    private Iterator<String> words;
    private boolean closed = false;

    @Override
    public boolean hasNext() {
      if (closed) {
        return false;
      }
      while (null == words || !words.hasNext()) {
        try {
          String nextLine = input.readLine();
          if (null != nextLine) {
            words = splitter.split(nextLine).iterator();
          } else {
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
        return words.next();
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
