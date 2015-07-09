package lamblin.common.source.line;

import java.io.InputStreamReader;

/**
 * Reads lines out of standard input.
 *
 * Created by dlamblin on 3/22/15.
 *
 * @author Daniel Lamblin
 */
public class InputStreamLineSource extends ReaderLineSource {

  public InputStreamLineSource() {
    super(new InputStreamReader(System.in), "Standard input stream");
  }
}
