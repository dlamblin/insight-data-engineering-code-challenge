package lamblin.common.runningmedian;

/**
 * Accepts entries to calculate the median over, and returns that median with each {@link #update}.
 *
 * @author Daniel Lamblin
 */
public interface RunningMedian<T> {

  /**
   * Adds the input into the set of values seen so far and outputs the median of that set.
   *
   * @param input the input value added to the growing set of values
   * @return the median of the set of values currently stored including the latest input
   */
  Double update(T input);
}
