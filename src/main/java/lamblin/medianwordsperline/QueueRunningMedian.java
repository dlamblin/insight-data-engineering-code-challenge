package lamblin.medianwordsperline;

import com.google.common.collect.MinMaxPriorityQueue;

/**
 * Keeps track of the running median and returns it on each update of the inputs so far.
 * This implementation inserts the new input into one of two {@link MinMaxPriorityQueue}s.
 * Adding to these is O(log n) while viewing or removing the min or max is O(1).
 * This is not concurrent.
 * <p/>
 * Created by dlamblin on 3/22/15.
 *
 * @author Daniel Lamblin
 */
public class QueueRunningMedian<T extends Number & Comparable<T>> implements RunningMedian<T> {

  private MinMaxPriorityQueue<T> smallHalf = MinMaxPriorityQueue.create();
  private MinMaxPriorityQueue<T> largeHalf = MinMaxPriorityQueue.create();

  @Override
  public double update(T input) {
    store(input);
    return median();
  }

  /**
   * While storing numbers we try to keep an equal number of elements in the small half and large
   * half of total elements and yet retain that all the small half elements are less than or equal
   * to all large half elements.
   * <p/>
   * This provides us with medians at the max of the small half and min of the large half.
   *
   * @param value the value to store in the collection(s) against which we keep a running median
   */
  private void store(T value) {
    if (smallHalf.isEmpty()) {
      // If we must add to the smaller half (implying the larger half is empty too)...
      smallHalf.add(value);
      return;
    }
    if (largeHalf.isEmpty()) {
      // If we must add to the large half...
      if (smallHalf.peekLast().compareTo(value) > 0) {
        // And if the max value in the smaller half is greater than the value to add...
        largeHalf.add(smallHalf.pollLast());
        smallHalf.add(value);
      } else {
        largeHalf.add(value);
      }
      return;
    }
    if (smallHalf.size() == largeHalf.size()) {
      // We can add to either half as they're the same size.
      if (smallHalf.peekLast().compareTo(value) >= 0) {
        // The max value in the small half is greater than or equal to the value so...
        smallHalf.add(value);
      } else {
        largeHalf.add(value);
      }
      return;
    }
    if (smallHalf.size() < largeHalf.size()) {
      // If we must add to the small half...
      if (smallHalf.peekLast().compareTo(value) >= 0) {
        // The max value in the small half is greater than or equal to the value so...
        smallHalf.add(value);
      } else {
        // Make the large half have the fewer elements by moving the minimum large element over
        smallHalf.add(largeHalf.pollFirst());
        largeHalf.add(value);
      }
    } else {
      // if we must add to the large half...
      if (smallHalf.peekLast().compareTo(value) >= 0) {
        // The max value in the small half is greater than or equal to the value so...
        // Make the small half have the fewer elements by moving the maximum small element over
        largeHalf.add(smallHalf.pollLast());
        smallHalf.add(value);
      } else {
        largeHalf.add(value);
      }
    }
  }

  /**
   * Based on how the elements are stored in two collections, the median is always either:
   * <ol>
   *   <li>The max of the small half and the min of the big half averaged together</li>
   *   <li>The max of the small half if it has one more element than the large half</li>
   *   <li>The min of the large half if it has one more element than the small half</li>
   * </ol>
   *
   * @return the current median
   */
  private Double median() {
    final int smallHalfSize = smallHalf.size();
    final int largeHalfSize = largeHalf.size();
    final long totalSize = smallHalfSize + largeHalfSize;
    if (totalSize == 0) {
      return null;
    }
    if (totalSize == 1) {
      return smallHalf.peekLast().doubleValue();
    }
    if (smallHalfSize == largeHalfSize) {
      return (smallHalf.peekLast().doubleValue()
              + largeHalf.peekFirst().doubleValue())
             / 2.0;
    }
    if (smallHalfSize < largeHalfSize) {
      return largeHalf.peekFirst().doubleValue();
    } else {
      // The small half has more elements, not equal elements due to previous if's return.
      return smallHalf.peekLast().doubleValue();
    }
  }
}
