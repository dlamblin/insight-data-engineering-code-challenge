package lamblin.common.source;

import java.io.InputStreamReader;

/**
 * Reads lines out of standard input.
 *
 * @author Daniel Lamblin
 */
public class InputStreamLineSource extends ReaderLineSource {

  /**
   * Opens stdin and reads lines out of it.
   */
  public InputStreamLineSource() {
    super(new InputStreamReader(System.in), "Standard input stream");
  }
}
