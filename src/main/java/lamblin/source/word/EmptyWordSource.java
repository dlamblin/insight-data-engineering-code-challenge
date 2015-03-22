package lamblin.source.word;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A representation of an empty input stream for a {@code WordSource}.
 *
 * Created by dlamblin on 3/21/15.
 *
 * @author Daniel Lamblin
 */
public class EmptyWordSource implements WordSource {

  @Override
  public Iterator<String> iterator() {
    return new innerIterator();
  }

  private class innerIterator implements Iterator<String> {

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public String next() {
      throw new NoSuchElementException("The EmptyWordSource contains no elements.");
    }
  }
}
