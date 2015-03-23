#!/usr/bin/perl
# The slightly better running median of word counts per line than the oneliner.
#
# Notes: I'm pretty sure perl has modules that bind a hash to a sorted keyset,
#        and also bind it to disk so that we can exit and resume the program.
#
# Daniel Lamblin

use warnings;
use strict;

# Establish a hash of counts and a total line count;
my %counts = ();
my $lines  = 0;

while(<>){			# For every line of input from all args as files
	$lines++;		# We will have an entry for this line.
	my $count = 0;		# It might be zero... but okay.
	foreach (split) {	# Each white-space delimited string in the line
		s/\W//g;	# - Remove all non-AlphaNumerics
		s/^\d*$//;	# - Remove any numbers (words of only digits)
		if(length) {	# - If the word isn't an empty string, count it
			$count++
		}
	}
	$counts{$count}++;	# Count one more where the hash-key is wordcount
	my @keys = sort(	# I'd like to eliminate this sort by
		keys %counts);	# binding to a hash with an always sorted keyset
				
				
	my $sum = 0;
	my $key;
	while($sum*2 < $lines	# I need to progress through the smallest half
		&& scalar @keys){	# of the counts
		$key = shift	# Take the next word count and sum its
			@keys; 	#   occurance count
		$sum += $counts{$key};
	}
	my $med = $key+0;	# Convert key to a number for the median
	if ($sum*2 == $lines) {	# We have an even number of lines and the next
		my $k = shift @keys;	# key (bucket) is also in the median
		$med = ($key+$k)/2;
	}
	printf("%.1f\n", $med);
}

