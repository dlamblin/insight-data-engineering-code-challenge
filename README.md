Insight Data Engineering Coding Challenge
=========================================

Last release: v2.0.1
--------------
### Once, current, and future state ###

- Working head of the master branch: [the version you're reading now]
  contains several solutions to the
  [challenge as updated on 2015-07-02][challenge-july]
  and some fixes to this readme.
- [v2.0.1][v2.0.1]: contains some documentation fixes and extra [JavaDoc][javadoc2]
  on the [`gh-pages` branch][gh-pages].
- [v2.0][v2.0]: represents several sollutions to the [challenge][#challenge-july].
  The submitted version was this tag v2.0. All commits back until v1.1 are
  breaking changes with respect to the earlier solution.
- [v1.1][v1.1]: represents the solution to the challenge as posted on 2015-03-17,
  linked at the bottom of this file.
- [v1.0][v1.0]: The solution as submitted the 2015-03-17 challenge will be
  retroactively tagged as v1.0.

Implemented versions overview
-----------------------------

- [_One-liner_](#one-liner)
 (Updated for v2.0)
- [_Perl_](#perl)
 (Updated for v2.0)
- [_Python_](#python)
 (Implemented for v2.0)
- [_Go_](#go)
 (Updated for v2.0)
- [_Java_](#java)
 (Updated for v2.0)

Refer to the [challenge overview ](#challenge-overview-for-the-unfamiliar) if
necessary.

Changes
-------

### From v2.0 to v2.0.1 ###
 - Fixes to [JavaDoc][javadoc2] for clarity, and publishing
   it on the [`gh-pages` branch][gh-pages].

### From v1.1 to v2.0 ###
- Total rewrite of the Perl and one-liner solutions.
- Addition of a Python solution.
- Large rewrite of the Go solution.
  - Concurrent with goroutines and channels.
  - Eliminated optional use of stopwords file.
- Large rewrite of the Java solution.
  - Combined the output of two features into a single run, slimming Gradle.
  - Fixed histogram running median implementation.
  - Removed the word input handling code for clarity.
  - Removed the word cleaner.
  - Eliminated optional use of stopwords file.
  - Reduced the use of Dagger to just one module. (stuck with v1 over v2)
  - Brought in a use of `AutoValue`.
- Some reorganization of the `src` directory, particularly for Java.
- The input and output directories and their contents were renamed and updated
  to match the revised challenge.
- This readme links to the appropriate commit of the challenge repository.
- The shell script no longer offers to download sample text from the Gutenberg
  project.

### From v1.0 to v1.1 ###
- A solution was added in the _Go_ language.
  - The _Go_ solution does not support reading whole directories unless they are
    piped into `stdin`. It does support stopwords.
- The shell script was updated to run this solution.

Shell script
------------
The file, `run.sh`, has its own usage information, which shows when
invoked with `h` or `help`. It can run either the _one-liner_, _Java_, _Perl_,
_Python_, or _Go_ version, and it can also clean the Java project.

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
[This solution's][.pl] running median uses an array where the index is the
number of unique words in a given tweet, and the value is how many tweets had
that number, making finding the median a $O(1)$ operation (best 0 worst 70).
The assumed range of unique words per tweet is from 0 to 69.

Python
------
The _Python_ [version of the solution][.py] takes advantage of the Python
[`Counter`][counter] subclass of dict. For its running median, it also uses
an array from 0 to 69 words per tweet as described above.
In an attempt to make use of multiple cores if available, it passes tweets
through a queue to worker processes which in turn hand off the two feature
statistics via two queues to an accumulating `Counter` and to a running median
calculator. As the order of tweets processed may vary, they are sequenced when
read and then ordered by a buffered resequencer for the running median.

Go
--
The _Go_ [solution][.go] uses goroutines and channels similarly to the way the _Python_ version passes queued work to workers.
It also uses a histogram approach to the running median problem. The solution
changed from the last challenge by taking a concurrent approach. Additionally it
no longer has to clean the words in the tweets nor does it support stop words.
It outputs directly to the two files specified as a single process.

Some tests were written for the _Go_ version such that it has 54.1% coverage of
`src/golang/tweetStats` and 60% coverage of `src/golang/util/file`.

Java
----
The _Java_ [solution][.java] is implemented similarly to the way the _Python_
and _Go_ were implemented. One pool of worker processes takes messages
concurrently, and then updates a word counting "bag" while sending unique word
counts into a queue, with their associated sequences. One thread in the worker
pool is reading out of the queue and resequencing the result as a running
median.

There's a little bit of added flexibility in the Java version where the user may
either pipe in tweets as lines on stdin, or specify any number of files on the
command line, preceded by `-i` for priority. If any of these files are
directories their direct contents are also processed as input.

Achieved using [JCommander][JCommander], the argument parsing also supports `-u`
flag which, via [Dagger][dagger], will swap out the implementation of the running
median from the histogram approach as described in the other solutions, to a
dual-min-max heap approach, where the ordered halves of inputs
are stored in equal sized heaps. Getting the max of the lesser heap and/or the
min of the greater heap allows for quickly outputting the current median.
Multiple parts of the solution were achieved with either [Guava][guava], or with
Java's [util.concurrent][concurrent] implementations.

[Gradle][gradle] was updated to v2.5, but the scope of custom tasks was greatly
reduced. I hope it doesn't seem over-engineered. There is some
[JavaDoc][javadoc1] for the earlier edition, and the [Gradle wrapper][gradlew]
can generate some [JavaDoc][javadoc2] for the current version too using the task
by that name. Additionally it can generate IntelliJ IDEA 14 CE compatible
projects files or if one were to add the eclipse plugin to build.gradle project
files for that ide. See the `$./gradlew tasks` output.

### Other Notes ###
The style of the _Java_ code tries to hew to the
[Google Java Style Guide][javastyle].

The arguments, input and output options are more flexible than the challenge
specified, reading directories, files, or stdin, and writing to files or
stdout. It may not seem like much compared to features built in _Perl_ and
_Python_, but _Java_ is a little messy in this area, and even `nio` and `nio2`
don't _truly_ help make file I/O code have a high degree of clarity.

Sample output is the same as shown in the challenge repository's readme.

Rough Timing on `data-gen` tweets.txt
-----------------------------------
Running on a 1.2mb file of tweets from the challenge's new `data-gen` directory I got the following; which I think showed there was some overhead to starting the jvm:

<pre>
$ time ./run.sh oneliner
... (the oneliner outputs to stdout)
~                           3
~30%                        11
~~                          1

real    0m5.755s
user    0m3.964s
sys     0m0.182s
$ time ./run.sh perl
You chose perl: from java, oneliner, perl, python, go, clean, or help.
See also the "oneliner" perl; and this source in src/perl
Running tweet stats for word count and running median.

real    0m0.295s
user    0m0.260s
sys     0m0.020s
$ time ./run.sh python
You chose python: from java, oneliner, perl, python, go, clean, or help.
The Python solution uses multiple worker processes.
Running tweet stats for word count and running median.

real    0m2.354s
user    0m3.448s
sys     0m0.573s
$ time ./run.sh go
You chose go: from java, oneliner, perl, python, go, clean, or help.
I assume you have go installed. You may need to set your GOPATH
Running tweet stats for word count and running median.

real    0m1.189s
user    0m0.526s
sys     0m0.363s
$ time ./run.sh java
You chose java: from java, oneliner, perl, python, go, clean, or help.
See all the source in src/java/{main,test}/lamblin
Running tweet stats for word count and running median.
Also try '-u' in run.sh to swap median implementation.

real    0m7.405s
user    0m3.388s
sys     0m0.638s
$ time ./run.sh java
You chose java: from java, oneliner, perl, python, go, clean, or help.
See all the source in src/java/{main,test}/lamblin
Running tweet stats for word count and running median.
Also try '-u' in run.sh to swap median implementation.

real    0m6.137s
user    0m3.048s
sys     0m0.430s
</pre>

Challenge Overview for the Unfamiliar
--------------------------------------
To learn what this challenge is about, please have a look at the
[challenge as updated][challenge-july] with [its FAQ][faq] etc.
as well as the older [challenge as posed][challenge]

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
Or 69, since 69 = 127 - 32 - 26, so there would be one non-unique "word" in a
tweet of all single character words.

##### Thanks to: #####
> [David Drummond][ddrum] for answering questions and making the challenge  
> the [Guava][guava] maintainers for such a breadth of performant time-savers  
> the [Dagger][dagger] maintainers for making DI less error-prone  
> the [JCommander][JCommander] maintainers for a flexible argument parser  
> the [Gradle][gradle] maintainers for their fine minimally-full-featured build-package-deploy tool-set  
> [JetBrains IDEA Community Edition][IDEA] contributors for the slickest IDE this side of a text editor  
> [Perl 5][perl] maintainers for keeping the tool, that can do these tasks in 1 to 40 lines, fast as ever  
> [Git][git] and [GitHub][github] for complementing each other and focusing on what you each do best  

###### - Daniel Lamblin ######

[v2.0.1]: https://github.com/dlamblin/insight-data-engineering-code-challenge/tree/v2.0.1 "at v2.0.1 Release"
[v2.0]: https://github.com/dlamblin/insight-data-engineering-code-challenge/tree/v2.0 "at v2.0 Release"
[v1.1]: https://github.com/dlamblin/insight-data-engineering-code-challenge/tree/v1.1 "at v1.1 Release"
[v1.0]: https://github.com/dlamblin/insight-data-engineering-code-challenge/tree/v1.0 "at v1.0 Release"
[challenge]: https://github.com/InsightDataScience/cc-example/tree/0d01fc8f703930ce522536230a3829d618f9fe99 "Insight Data Science Coding Challenge Example"
[challenge-july]: https://github.com/InsightDataScience/cc-example/tree/1eb0b6e398c0ad069436e65f90dc6285c319acc1 "Insight Data Science Coding Challenge Example"
[faq]: https://github.com/InsightDataScience/cc-example#faq "Challenge FAQ"
[oneliner]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/perl/non-scalable_10min_oneliner.pl
[.pl]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/perl/tweetStats.pl "Perl running median unique words per line and word count"
[.py]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/python/tweetStats.py "Python running median unique words per line and word count"
[counter]: https://docs.python.org/3.4/library/collections.html#collections.Counter "A Counter is a dictionary where key counts are stored as the key values"
[.go]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/golang/tweetStats/tweetStats.go "Go running median unique words per line and word count"
[.java]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/java/main/lamblin/tweetstats/TweetStatsCmd.java "Java running median unique words per line and word count"
[JCommander]: http://jcommander.org "JCommander - Because life is too short to parse command line parameters"
[dagger]: http://square.github.io/dagger/ "Dagger"
[guava]: https://github.com/google/guava "com.google.common"
[concurrent]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/package-summary.html "java.util.concurrent"
[gradle]: http://gradle.org/getting-started-gradle-java/ "Open source build automation"
[gradlew]: https://spring.io/guides/gs/gradle/#_build_your_project_with_gradle_wrapper "The preferred way of starting a Gradle build"
[javastyle]: https://google-styleguide.googlecode.com/svn/trunk/javaguide.html "Google Java Style"
[run]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/run.sh "run.sh"
[shellstyle]: https://google-styleguide.googlecode.com/svn/trunk/shell.xml "Google shell style guide"
[javadoc1]: http://dlamblin.github.io/insight-data-engineering-code-challenge/javadoc_v1.0/index.html "JavaDoc v1.0"
[javadoc2]: http://dlamblin.github.io/insight-data-engineering-code-challenge/javadoc_v2.0/index.html "JavaDoc v2.0"
[gh-pages]: http://dlamblin.github.io/insight-data-engineering-code-challenge/index.html "Quick github.io page about this"
[ddrum]: https://github.com/ddrum001 "David E Drummond on GitHub"
[IDEA]: https://www.jetbrains.com/idea/features/editions_comparison_matrix.html "Compare IDEA CE and Ultimate"
[perl]: https://www.perl.org/about.html "Six made Five better"
[git]: http://git-scm.com/book/en/v2/Getting-Started-Git-Basics "Mercurial is also worth considering..."
[github]: https://github.com "You are here, unless... well you checked out this repo."
