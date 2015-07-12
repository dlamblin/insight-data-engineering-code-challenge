package lamblin.common.source;

import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReaderLineSourceTest {

  private Iterator<String> iterator;

  @Before
  public void setUp() {
    iterator = new ReaderLineSource(
        new StringReader("It's a string\nwith some\tTEXT."),
        "Test string").iterator();
  }

  @Test
  public void testIterator() throws Exception {
    assertTrue(iterator.hasNext());
    int contents = 0;
    String[] expected = {"It's a string", "with some\tTEXT."};
    String[] results = new String[expected.length];
    while (iterator.hasNext()) {
      results[contents++] = iterator.next();
    }
    assertEquals(expected.length, contents);
    assertArrayEquals(expected, results);
  }
}