package lamblin.common.runningmedian;

/**
 * Accepts entries to calculate the median over, and returns that median.
 *
 * @author Daniel Lamblin
 */
public interface RunningMedian<T> {
  double update(T input);
}
