Insight Data Engineering Coding Challenge
=========================================

Once, current, and future state
-------------------------------

- Working head of the master branch: [the version you're reading now]
contains progress towards solving the
[challenge as updated on 2015-07-02](#challenge-july). Once ready the submitted
version will be tagged as v2.0. All commits after v1.1 are breaking changes with
respect to the earlier solution.
- v1.1: represents the solution to the challenge as posted on 2015-03-17,
linked at the bottom of this file. *Please checkout this tag as the latest
working version*.
- v1.0: The solution as submitted the 2015-03-17 challenge will be retroactively
tagged as v1.0.

Implemented versions overview
-----------------------------

- [_One-liner_](#one-liner)
 (Refers to v1.1 - Broken)
- [_Perl_](#perl)
 (Refers to v1.1 - Broken)
- [_Python_](#python)
 (Forthcoming)
- [_Go_](#go)
 (Refers to v1.1 - Broken)
- [_Java_](#java)
 (Refers to v1.1 - Broken)

Refer to the
[challenge overview ](#challenge-overview-for-the-unfamiliar)
if necessary.

Changes
-------

### From v1.1 towards v2.0 ###
- This readme links to the appropriate commit of the challenge repository.
- The shell script no longer offers to download sample text from the Guttenburg
  project.
- The input and output directories and their contents were renamed and updated
  to match the revised challenge.
- Total rewrite of the Perl and oneliner solutions.
- The following are speculated changes:
  - Total rewrites of the Go solutions.
  - Addition of a Python solution.
  - Large rewrite of the Java solution.
    - Removed the input handling code for clarity and relying on the shell
      script to correctly pipe in files.
    - Removed the word cleaner.
    - Removed the queue (min-max heap) based running median in favor of the
      fixed histogram running median.
    - Combined the output of two features into a single run.

### From v1.0 to v1.1 ###
- A solution was added in the _Go_ language.
  - The _Go_ solution does not support reading whole directories unless they are
    piped into `stdin`. It does support stopwords.
- The shell script was updated to run this solution.

Shell script
------------
The file, `run.sh`, has its own usage information, which shows when
invoked with `h` or `help`. It can run either the _oneliner_, _Java_, or _Perl_
version, and it can also clean the project.

Please do [read through the script][run]; it is also mostly in the
[Google shell style][shellstyle]. As per the challenge, it is the primary
method of using the solutions in this repository.

One-liner
----------
The Perl one-liner [`non-scalable_10min_oneliner.pl`][oneliner] was the first
code I produced to answer the challenge, in one-liner form.
The one-liner does not output to the files but rather to STDOUT. It's running
median method is naive in that it sorts an array of all the unique counts seen
for each tweet. This turns out to be slow, because the sorting is
$O(n \log n)$ every time something is added, to give the median an $O(1)$
look-up performance after the sort. This is a total of so $O(n \log n)$ for
each tweet, growing with each tweet processed. It is a starting point for
the other attempted solutions.

Perl
----
The _Perl_ version was a rewrite, discarding the one-liner approach.
I hope the comments keep it readable.
This [solution's][Pelr] running median uses an array where the index is the
number of unique words in a given tweet, and the value is how many tweets had
that number, making finding the median a $O(1)$ operation (best 0 worst 70).
The assumed range of unique words per tweet is from 0 to 69.

Python
------
Forthcoming;
The _Python_ version of the solution is in the same vein as the _Perl_ version.

Go
--
The _Go_ version was added well after the completion of the challenge. It
uses only a histogram approach to the running median problem. While there's
opportunities to parallelize the processing in Go, I haven't as such done so as
both updating the word count map and updating the counts per line seen would
need to be locked from concurrent updates by the worker pool of goroutines.
There's a possibility that the benefits of letting goroutines handle the string
and regular expression matching would help in utilizing all available cores.

Java
----
While I originally thought to move on to writing this in a couple of languages,
_Python_ and _Go_ sprang to mind, I wanted to commit to also adding in _Java_
code. Knowing that _Java_ is verbose, and trying out three new libraries and
tools, I was a bit new to: Gradle, Dagger, and JCommander, I spent all my
available time on this language's version. I hope it doesn't seem
over-engineered. There is some [JavaDoc][javadoc] for it.

### Trade-offs ###

#### Skipped tuning JVM ####
I have not tuned any of the jvm properties to allocate more memory or tune
garbage collection. If the inputs were truly large, that will be
necessary for the user to do.

#### Inversion of control ####
The dependency injection allows me to, for example, swap out the
range-limited frequency based running median class, [RangeRunningMedian][RRM],
with a dual-heap based approach, [QueueRunningMedian][QRM], for median
processing which is not limited in input range, but which is rather hindered
by needing to store each word count for each line. You can try it out with
the `-u` option. See also `-h`.

#### Compile time injection ####
The dependency injection is checked at compile time and largely configured then,
so it doesn't cause a bunch of overhead at startup. It might not be used
idiomatically for [Dagger][dagger] yet, some cleanup of its modules seems to be
in order as I learn more about Dagger and continue to use it.

#### Supporting stop words ####
Both the word count and running median tool will ignore empty words, numbers,
or words made of punctuation like `---`. Additionally they can ignore words
listed in a stopwords file, or directory of such files (but not `stdin`). Both
challenge questions share the same common word filter called [WordCleaner][WC].

#### Concurrent word counting ####
The word counting accumulator [WordAccumulator][WA] was implemented with a
concurrent multiset. This allows it to have a thread pool updating the multiset
as words come in, and then terminate when the set's results are requested. The
accumulator maintains a separate concurrently sorted set of words so that the
output can be shown in order. This uses more memory overall. It works pretty
quickly, but I had two other versions I wanted to develop, time permitting:

- Using a tree multiset would maintain its key set in sorted order, thus using
  less memory (probably) than it does now. However, a quick test of dropping it
  in showed it wasn't concurrent, so it remains to be seen if it would compete
  with the threaded version. There is a chance that for normal size corpuses
  the overhead of managing a threadpool is more significant than the savings
  of bringing more cpu cores to bear on the work.
- A trie based accumulator. I had a _C#_ trie class (written during a phone
  interview) but not on hand. It would seem to be more memory efficient,
  and also allow for alphabetic traversal (pre-order) easily.
  With input and retrieval being $O(1)$ regarding the number of words stored or
  $O(n)$ regarding the length of the word being stored or retrieved, it should
  perform better than the other sorting approaches.

#### Maintainable code ####
The verbosity of the input and output handling allow for some further
development and expansion of scope, like optional median and word
accumulators. E.G. doing a running median of a different statistic per line
than word count would be a matter of swapping in a different
transformation function than the [WordCountTransformer][WCT] in the
configuration module [RunningMedianModule][RRM].

#### Testing ####
While there is a structure for testing the _Java_ code with `jUnit4`, few
classes underwent testing. Dependency injection should help make the
whole project testable, but I traded test writing off for
more development time. There are tests for the most important WordSources.

#### Functional brevity, and directory order ####
The [Guava][guava] functional approach was used when reading directories. This
allows the directory readers to be composed of chained file readers, which in
turn (with standard input readers) are based on just the main logic in the
Reader*Source. The warning in Guava about functional programming being
unclear still allows for its use when there's a significant reduction in lines
thus aiding readability. It should be noted that the DirectoryStream claims
to be unordered but always behaved in alphabetic order on my system, prompting
me to drop circling back to sort the directory listing for the running median
in the interest of time.

### Other Notes ###
The style of the _Java_ code tries to hew to the
[Google Java Style Guide][javastyle].

The arguments, input and output options are more flexible than the challenge
specified, reading directories, files, or stdin, and writing to files or
stdout. It may not seem like much compared to features built in _Perl_ and
_Python_, but _Java_ is a little messy in this area, and even `nio` and `nio2`
don't _truely_ help make file io code have a high degree of clarity.

Sample output on sample.txt
---------------------------
Once the `run.sh java` script has finished with `./gradlew installApp`, you
could manually skip using `run.sh` and use the programs directly as in the below
demonstration:

    $

Challenge Overview for the Unfamiliar
--------------------------------------
To learn what this challenge is about, please have a look at the
[challenge as posed][challenge] with [its FAQ][faq] etc.

### Synopsis ###
The goal is to ingest tweet-like text and to

1. Tabulate the number of times each word has been tweeted across all tweets
   seen.
2. Maintain a median of the number of unique words per tweet, which is updated
   and output with each new tweet.

For this certain assumptions and definitions are made:
- The input tweets are just `ASCII`, specifically containing only lowercase
  letters, numbers, and printable "characters like ':', '@', '#'."
- Any whitespace separates words.
- As tweets are 140 characters, a maximum of 70 unique words per tweet exists.
Or 69, since 69 = 127 - 32 - 26, so there would be one non-uniqe "word" in a
tweet of all single character words.

##### Thanks to: #####
> Anne Bessman and David Drummond for answering questions and making the challenge  
> the Guava maintainers for such a breadth of performant time-savers  
> the Dagger maintainers for making DI less error-prone  
> the JCommander maintainers for a flexible argument parser  
> the Gradle maintainers for their fine minimally-full-featured build-package-deploy toolset  
> JetBrains Community Edition contributors for the slickest IDE this side of a text editor  
> Perl 5 maintainers for keeping the tool, that can do these tasks in 1 to 40 lines, fast as ever  
> Git and GitHub for complementing each other and focusing on what you each do best  

###### - Daniel Lamblin ######

[challenge]: https://github.com/InsightDataScience/cc-example/tree/0d01fc8f703930ce522536230a3829d618f9fe99 "Insight Data Science Coding Challenge Example"
[challenge-july]: https://github.com/InsightDataScience/cc-example/tree/1eb0b6e398c0ad069436e65f90dc6285c319acc1 "Insight Data Science Coding Challenge Example"
[faq]: https://github.com/InsightDataScience/cc-example#faq "Challenge FAQ"
[oneliner]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/perl/non-scalable_10min_oneliner.pl
[RRM]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/java/main/lamblin/medianwordsperline/RangeRunningMedian.java "RangeRunningMedian"
[QRM]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/java/main/lamblin/medianwordsperline/QueueRunningMedian.java "QueueRunningMedian"
[WC]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/java/main/lamblin/common/source/word/filter/WordCleaner.java "WordCleaner"
[WA]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/java/main/lamblin/wordcount/WordAccumulator.java "WordAccumulator"
[WCT]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/java/main/lamblin/medianwordsperline/WordCountTransformer.java "WordCountTransformer"
[RMM]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/java/main/lamblin/medianwordsperline/RunningMedianModule.java "RunningMedianModule"
[Perl]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/perl/tweetStats.pl "Perl running median unique words per line and word count"
[dagger]: http://square.github.io/dagger/ "Dagger"
[guava]: https://github.com/google/guava "com.google.common"
[javastyle]: https://google-styleguide.googlecode.com/svn/trunk/javaguide.html "Google Java Style"
[run]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/run.sh "run.sh"
[shellstyle]: https://google-styleguide.googlecode.com/svn/trunk/shell.xml "Google shell style guide"
[javadoc]: http://dlamblin.github.io/insight-data-engineering-code-challenge/javadoc/index.html "JavaDoc"
