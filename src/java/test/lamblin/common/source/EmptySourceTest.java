package lamblin.common.source;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.NoSuchElementException;

import lamblin.common.source.EmptySource;

import static org.junit.Assert.*;

public class EmptySourceTest {

  private EmptySource emptySource;

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    emptySource = new EmptySource();
  }

  @Test
  public void testHasNext() {
    assertFalse(emptySource.iterator().hasNext());
  }

  @Test
  public void testNext() {
    thrown.expect(NoSuchElementException.class);
    emptySource.iterator().next();
  }
}