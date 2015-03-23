package lamblin.medianwordsperline;

import java.io.PrintStream;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.ObjectGraph;
import lamblin.common.Module;
import lamblin.common.source.line.LineSource;

/**
 * The Insight Data Engineering Coding Challenge issued 2015-03-17
 * The Java version.
 * <p/>
 * Second part, running median of words per line.
 * <p/>
 * Created by dlamblin on 3/22/15.
 *
 * @author Daniel Lamblin
 */
public class RunningMedianWordsPerLineCmd {

  @Inject
  @Named("input")
  LineSource lineSource;

  @Inject
  PrintStream output;

  @Inject
  RunningMedian<String> runningMedian;

  public static void main(String[] args) {
    // Setup injection based on arguments
    ObjectGraph objectGraph = ObjectGraph.create(new RunningMedianModule(), new Module(args));
    RunningMedianWordsPerLineCmd perLineCmd = objectGraph.get(RunningMedianWordsPerLineCmd.class);

    // Start counting words per line
    perLineCmd.countWordsPerLine();
  }

  private void countWordsPerLine() {
    for (String line : lineSource) {
      output.printf("%.2f\n", runningMedian.update(line));
    }
    output.close();
  }
}
