package lamblin.common.source.word;

import java.io.InputStreamReader;

/**
 * Reads words out of standard input.
 *
 * Created by dlamblin on 3/21/15.
 *
 * @author Daniel Lamblin
*/
public class InputStreamWordSource extends ReaderWordSource {

  public InputStreamWordSource() {
    super(new InputStreamReader(System.in), "Standard input stream");
  }
}
