package lamblin.medianwordsperline;

import com.google.common.base.Function;

/**
 * A {@link RunningMedian} encapsulation that transforms its {@link #update} method inputs for
 * correct storage in the constructor parameter's provided {@link RunningMedian} implementation.
 * <p/>
 * Created by dlamblin on 3/23/15.
 *
 * @author Daniel Lamblin
 */
public class TransformingRunningMedian<T, E extends Number & Comparable<E>>
    implements RunningMedian<T> {

  private Function<T, E> transformer;
  private RunningMedian<E> runningMedian;

  public TransformingRunningMedian(Function<T, E> transformer, RunningMedian<E> runningMedian) {
    this.transformer = transformer;
    this.runningMedian = runningMedian;
  }

  @Override
  public double update(T input) {
    return runningMedian.update(transformer.apply(input));
  }
}
