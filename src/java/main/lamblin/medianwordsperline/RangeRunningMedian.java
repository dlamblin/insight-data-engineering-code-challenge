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
 * Created by dlamblin on 3/23/15.
 *
 * @author Daniel Lamblin
 */
 public class RangeRunningMedian<T extends Number & Comparable<T>> implements RunningMedian<T> {

  private final HashMap<T, Long> inputCounts;
  private final Long minimumIncluded;
  private final Long maximumIncluded;
  private final Long finestInterval;
  private long size;

  /**
   * Will allocate a number of buckets, like a histogram, starting at {@code minimumIncluded},
   * increasing by {@code finestInterval} until {@code maximumIncluded}. All updates must have
   * input values which fall in these buckets.
   *
   * @param minimumIncluded the smallest value an update can be
   * @param maximumIncluded the largest value an update can be
   * @param finestInterval the distribution of values between these; recommend integer like 1.
   */
  public RangeRunningMedian(Long minimumIncluded, Long maximumIncluded, Long finestInterval) {
    this.size = 0;
    this.minimumIncluded = minimumIncluded;
    this.maximumIncluded = maximumIncluded;
    this.finestInterval = finestInterval;
    Long longSize = ((maximumIncluded - minimumIncluded) / finestInterval) + 1;
    if (longSize > Integer.MAX_VALUE) {
      // This probably won't fit in a HashMap, but we'll try.
      System.err.printf(
          "Error: Range from %d to %d by %d is too big for the max number of buckets\n",
          minimumIncluded, maximumIncluded, finestInterval);
    }
    int size = longSize.intValue();
    inputCounts = new HashMap<>(size);
  }

  /**
   * Increment the count at the input's bucket, and return a median.
   *
   * @param input the value to add
   * @return the median of all values added thus far
   */
  @Override
  public double update(T input) {
    if (input.longValue() < minimumIncluded || input.longValue() > maximumIncluded) {
      throw new IllegalArgumentException(
          "The input falls outside the range given and construction time");
    }
    inputCounts.put(input, inputCounts.getOrDefault(input, 0L) + 1L);
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
    Long currentNumber = minimumIncluded;
    while (currentNumber.compareTo(maximumIncluded) <= 0
           && 2 * count < size) {
      count += inputCounts.getOrDefault(currentNumber, 0L);
      currentNumber = currentNumber + finestInterval;
    }
    if (size % 2 != 0) {
      return currentNumber - finestInterval;
    } else {
      if (2 * count > size) {
        return currentNumber - finestInterval;
      } else {
        return (currentNumber + currentNumber - finestInterval) / 2.0;
      }
    }
  }
}
