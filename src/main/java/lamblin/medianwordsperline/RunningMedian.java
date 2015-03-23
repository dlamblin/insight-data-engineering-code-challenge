package lamblin.medianwordsperline;

/**
 * Accepts entries to calculate the median over, and returns that median.
 *
 * Created by dlamblin on 3/22/15.
 *
 * @author Daniel Lamblin
 */
public interface RunningMedian<T> {
  double update(T input);
}
