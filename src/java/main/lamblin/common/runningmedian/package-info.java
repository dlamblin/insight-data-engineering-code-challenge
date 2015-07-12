/**
 * This package contains implementations of a running median.
 * <dl>
 *   <dt>{@link lamblin.common.runningmedian.RunningMedian}</dt>
 *   <dd>An interface for the implementation of a median that updates with each addition of a value.
 *   </dd>
 *   <dt>{@link lamblin.common.runningmedian.QueueRunningMedian}</dt>
 *   <dd>Stores all the numbers seen in each
 *       {@link lamblin.common.runningmedian.RunningMedian#update(java.lang.Object)}
 *       in a balance pair of lesser and greater heaps (implemented via
 *       {@link com.google.common.collect.MinMaxPriorityQueue}) outputing the median by either
 *       averaging the max of the lesser heap with the min of the greater heap, or by picking the
 *       same from the one heap with one more member than the other heap.
 *   </dd>
 *   <dt>{@link lamblin.common.runningmedian.RangeRunningMedian}</dt>
 *   <dd>When the range of possible values is known in advance and are integers, the range running
 *   median can more quickly and more compactly store the seen set as a histogram and output the
 *   median by travelling over the sum of about half of the number of values counted.</dd>
 * </dl>
 *
 * @author Daniel Lamblin
 */
package lamblin.common.runningmedian;