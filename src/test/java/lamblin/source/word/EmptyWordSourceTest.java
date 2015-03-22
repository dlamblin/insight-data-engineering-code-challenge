package lamblin.source.word;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class EmptyWordSourceTest {

  private EmptyWordSource emptyWordSource;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    emptyWordSource = new EmptyWordSource();
  }

  @Test
  public void testHasNext() {
    assertFalse(emptyWordSource.iterator().hasNext());
  }

  @Test
  public void testNext() {
    thrown.expect(NoSuchElementException.class);
    emptyWordSource.iterator().next();
  }
}