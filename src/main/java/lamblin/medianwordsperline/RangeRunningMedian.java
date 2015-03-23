package lamblin.medianwordsperline;

import java.util.HashMap;

/**
 * Updates a running median for all the data input with each update of added data.
 * <p/>
 * If we know the range of possible integer inputs to update, as we were told that there would
 * never
 * be more than 50 words per line, then we can, unlike the {@link QueueRunningMedian}, not store
 * each input, but rather the number of times each possible input in the range has been seen.
 * <p/>
 * Combined with a total of how many inputs have been seen, we need only count half that total into
 * the sums from the beginning of the range onwards, identifying the mean in constant O(1) time.
 * It's also possible to store the data in constant O(1) time. Memory use remains bounded to the
 * size of range number of inputs.
 * <p/>
 * Limitations are that the number of inputs total should be less than a long's maximum value and
 * similarly that the number of any one integer value inputs should be less as well. Also the number
 * of buckets should be less than integer's maximum value.
 * <p/>
 * TODO(lamblin): The ugly static constructors and abstract methods are all due to bad generics.
 * TODO(lamblin): It could be removed if one class using Longs were extended by one using Integers.
 * Created by dlamblin on 3/23/15.
 *
 * @author Daniel Lamblin
 */
 abstract class RangeRunningMedian<T extends Number & Comparable<T>> implements RunningMedian<T> {

  private final HashMap<T, Long> inputCounts;
  private final T minimumIncluded;
  private final T maximumIncluded;
  private final T finestInterval;
  private long size;

  public static RangeRunningMedian<Long> newLongRangeRunningMedian(long min, long max, long ivl) {
    return new RangeRunningMedian<Long>(min, max, ivl) {
      @Override
      protected double toDouble(Long a) {
        return a.doubleValue();
      }

      @Override
      protected int toInt(Long a) {
        return a.intValue();
      }

      @Override
      protected Long sub(Long a, Long b) {
        return a - b;
      }

      @Override
      protected Long add(Long a, Long b) {
        return a + b;
      }

      @Override
      protected Long div(Long a, Long b) {
        return a / b;
      }
    };
  }

  public static RangeRunningMedian<Integer> newIntRangeRunningMedian(int min, int max, int ivl) {
    return new RangeRunningMedian<Integer>(min, max, ivl) {
      @Override
      protected double toDouble(Integer a) {
        return a.doubleValue();
      }

      @Override
      protected int toInt(Integer a) {
        return a.intValue();
      }

      @Override
      protected Integer sub(Integer a, Integer b) {
        return a - b;
      }

      @Override
      protected Integer add(Integer a, Integer b) {
        return a + b;
      }

      @Override
      protected Integer div(Integer a, Integer b) {
        return a / b;
      }
    };
  }

  /**
   * Will allocate a number of buckets, like a histogram, starting at {@code minimumIncluded},
   * increasing by {@code finestInterval} until {@code maximumIncluded}. All updates must have
   * input values which fall in these buckets.
   *
   * @param minimumIncluded the smallest value an update can be
   * @param maximumIncluded the largest value an update can be
   * @param finestInterval the distribution of values between these; recommend integer like 1.
   */
  private RangeRunningMedian(T minimumIncluded, T maximumIncluded, T finestInterval) {
    this.size = 0;
    this.minimumIncluded = minimumIncluded;
    this.maximumIncluded = maximumIncluded;
    this.finestInterval = finestInterval;
    int size = toInt(div(sub(maximumIncluded, minimumIncluded), finestInterval)) + 1;
    inputCounts = new HashMap<>(size);

    T currentNumber = minimumIncluded;
    for (int i = 0; i < size; i++) {
      inputCounts.put(currentNumber, 0L);
      currentNumber = add(currentNumber, finestInterval);
    }
  }

  /**
   * Increment the count at the input's bucket, and return a median.
   *
   * @param input the value to add
   * @return the median of all values added thus far
   */
  @Override
  public double update(T input) {
    if (!inputCounts.containsKey(input)) {
      throw new IllegalArgumentException(
          "The input falls outside the range given and construction time");
    }
    inputCounts.put(input, inputCounts.get(input) + 1L);
    size++;
    return median();
  }

  /**
   * Calculate the median by counting all the counts in order from minimum towards maximum until
   * reaching just under half way to the full number of elements counted.
   *
   * @return a median of all the values updated so far
   */
  private double median() {
    long count = 0;
    T currentNumber = minimumIncluded;
    while (currentNumber.compareTo(maximumIncluded) <= 0
           && 2 * count < size) {
      count += inputCounts.get(currentNumber);
      currentNumber = add(currentNumber, finestInterval);
    }
    if (size % 2 != 0) {
      return toDouble(sub(currentNumber, finestInterval));
    } else {
      if (2 * count > size) {
        return toDouble(sub(currentNumber, finestInterval));
      } else {
        return toDouble(sub(add(currentNumber, currentNumber), finestInterval)) / 2.0;
      }
    }
  }
  protected abstract double toDouble(T a);
  protected abstract int toInt(T a);
  protected abstract T sub(T a, T b);
  protected abstract T add(T a, T b);
  protected abstract T div(T a, T b);
}
