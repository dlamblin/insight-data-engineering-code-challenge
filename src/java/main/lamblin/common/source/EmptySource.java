package lamblin.common.source;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A representation of an empty input stream for {@link LineSource}.
 *
 * @author Daniel Lamblin
 */
public class EmptySource implements LineSource {

  /**
   * @return an iterator where {@link innerIterator#hasNext()} is always {@code false}
   */
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
      throw new NoSuchElementException("The EmptySource contains no elements.");
    }
  }
}
