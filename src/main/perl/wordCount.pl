#!/usr/bin/perl
# The slightly better word counter than the oneliner.
#
# Notes: I'm pretty sure perl has modules that bind a hash to a sorted keyset.
#        I did not find the method of doing that though.
#
# Daniel Lamblin

use warnings;
use strict;

my %count=();			# Establish a hash of words to their count
while(<>) {			# Over each line from all files in the args
	foreach (split){	# Split into whitespace delimited words
		s/\W//g;	# Remove non-AlphaNumerics from words
		s/^\d*$//;	# Remove if only digits in the words
		if(length) {	# Count only non-empty words
		$count{lc()}++;	# Lower-case the index to count the word
		}
	}
}				# Surprise, we're done parsing input
foreach my $word (sort keys(%count)) {	# Asciibetically order the words
	printf "%-15s\t%d\n",	# Output the desired, "word tab count" view
		$word, $count{$word};
}
#This seems to time better than the Java... Less overhead maybe?
