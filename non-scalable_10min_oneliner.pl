use warnings;use strict;undef $/;my %count=();$_=<>;foreach (split){s/\W//g;$count{lc()}++;};foreach my $word (sort keys(%count)){printf "%-16s%d\n", $word, $count{$word};}
