#!/usr/bin/perl
# Better than the oneliner; this running median uses the histrogram approach to
# tacking the set of the number of unique words per tweet seen so far. As tweets
# are limited to 140 characters they can have at most 70 "words" on a line.
# Given the limited character set however, only 69 unique words could fit in a
# tweet.
# At the same time this script tracks the count of words seen accross tweets.
# The output of both of these is respectively written to ft2.txt and ft1.txt.
#
# Tweets are sent in using STDIN or in files specified as command arguments and
# output is written to files noted above in the directory "./tweet_output/" or
# the directory specified by the -o argument.
#
# Note: This assumes the input is as specified by the challenge:
# Only printable ascii, up to 140 character per line, no uppercase letters.
# --
# Daniel Lamblin

use warnings;
use strict;

sub setup_output_files {
  # Read command line option -o if available or default to "./tweet_output"
  my %opts = ('o' => './tweet_output');
  use Getopt::Std;
  getopt('o', \%opts);

  my $dir = $opts{'o'};
  die "ERROR: Directory \"$dir\" doesn't exist."     unless (-e $dir);
  die "ERROR: Directory \"$dir\" isn't a directory." unless (-d $dir);
  die "ERROR: Directory \"$dir\" isn't writable."    unless (-w $dir);

  local (*F1, *F2);
  my ($p1, $p2) = ("> $dir/ft1.txt", "> $dir/ft2.txt");
  die "ERROR: Couldn't open \"$p1\" for output." unless open(F1, $p1);
  die "ERROR: Couldn't open \"$p2\" for output." unless open(F2, $p2);
  return ($dir, *F1, *F2);
}

# Establish an array of 70 unique word counts and a total line count;
my @counts = (0)x70;
my $lines  = 0;
# Setup a hash of words counted accross tweets seen
my %words = ();
# Open ft1.txt and ft2.txt for output.
my ($output_dir, $F1, $F2) = setup_output_files();

while (<>) {           # For every line of input from all args as files
  $lines++;            # We will have an entry for this line.
  my %unique = ();     # In each tweet we count only unique words.
  foreach (split) {    # Each white-space delimited string in the line
    if (length) {      # - If the word isn't an empty string:
      $unique{$_} = 1; # - Note it for the per-tweet unique words, and
      $words{$_}++;    # - Count it as a pan-tweet word instance.
    }
  }
  $counts[scalar keys %unique]++; # This tweet has (scalar keys %unique) words

  # Now figure out the median of all the unique word counts per tweet seen.
  my $sum = 0;
  my $i   = 0;
  while (
    $sum * 2 < $lines  # I need to progress through the smallest half
    && $i < 70         # of the counts
  ) {
    $sum += $counts[$i++]; # sum up the counts from 0 to 69 unique words.
  }
  my $med = $i - 1;    # We've found the median unless the next clause is true
  if ( $sum * 2 == $lines ) { # We have an even number of lines and the next
      while ($counts[$i] == 0 && $i < 70) {
        $i++;          # Establish that the next count of words exists.
      }
      $med = ( $med + $i ) / 2.0;
  }
  printf( $F2 "%.2f\n", $med ); # The running medians go into ft2.txt
}
close($F2);

# Now that we're done with tweets we'll output the whole set of words counted.
foreach (sort keys %words) {
  printf( $F1 "%-27s %d\n", $_, $words{$_});
}
close($F1);
