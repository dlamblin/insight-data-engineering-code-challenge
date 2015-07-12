package lamblin.tweetstats;

import com.google.auto.value.AutoValue;

/**
 * Represents a sequence number and a count of unique words in the message that shared that sequence
 * number. Since messages are processed by pooled worker threads, the output counts from these
 * workers might be put into the queue in a different order than they came in.
 * Thus they are identified with a sequence number for the running median to output correctly.
 */
@AutoValue
abstract class SequencedCount {

  static SequencedCount create(int sequence, int count) {
    return new AutoValue_SequencedCount(sequence, count);
  }

  /**
   * The package-private no-op constructor is recommended as a best practice with AutoValue.
   */
  SequencedCount(){}

  abstract int sequence();
  abstract int count();
}