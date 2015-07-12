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
 (Updated for v2)
- [_Perl_](#perl)
 (Updated for v2)
- [_Python_](#python)
 (Implemented for v2)
- [_Go_](#go)
 (Updated for v2)
- [_Java_](#java)
 (Refers to v1.1 - Broken)

Refer to the
[challenge overview ](#challenge-overview-for-the-unfamiliar)
if necessary.

Changes
-------

### From v1.1 towards v2.0 ###
- This readme links to the appropriate commit of the challenge repository.
- The shell script no longer offers to download sample text from the Gutenberg
  project.
- The input and output directories and their contents were renamed and updated
  to match the revised challenge.
- Total rewrite of the Perl and one-liner solutions.
- Addition of a Python solution.
- Large rewrite of the Go solution.
- The following are speculated changes:
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
This [solution's][Perl] running median uses an array where the index is the
number of unique words in a given tweet, and the value is how many tweets had
that number, making finding the median a $O(1)$ operation (best 0 worst 70).
The assumed range of unique words per tweet is from 0 to 69.

Python
------
The _Python_ version of the solution takes advantage of the Python
[`Counter`][counter] subclass of dict. For its running median, it also uses
an array from 0 to 69 words per tweet as described above.
In an attempt to make use of multiple cores if available, it passes tweets
through a queue to worker processes which in turn hand off the two feature
statistics via two queues to an accumulating `Counter` and to a running median
calculator. As the order of tweets processed may vary, they are sequenced when
read and then ordered by a buffered resequencer for the running median.

Go
--
The _Go_ version uses goroutines and channels similarly to the way the _Python_
version passes queued work to workers.
It also uses a histogram approach to the running median problem.
The solution changed from the last challenge by taking a concurrent approach.
Additionally it no longer has to clean the words in the tweets nor does it
support stop words. It gained output directly to the two files specified as a
single process.

Java
----
This paragraph dates from v1.0. There is no current _Java_ solution to the
updated challenge. It's likely for v2.0 I should remove the input processing.

While I originally thought to move on to writing this in a couple of languages,
_Python_ and _Go_ sprang to mind, I wanted to commit to also adding in _Java_
code. Knowing that _Java_ is verbose, and trying out three new libraries and
tools, I was a bit new to: Gradle, Dagger, and JCommander, I spent all my
available time on this language's version. I hope it doesn't seem
over-engineered. There is some [JavaDoc][javadoc] for it.

### Other Notes ###
The style of the _Java_ code tries to hew to the
[Google Java Style Guide][javastyle].

The arguments, input and output options are more flexible than the challenge
specified, reading directories, files, or stdin, and writing to files or
stdout. It may not seem like much compared to features built in _Perl_ and
_Python_, but _Java_ is a little messy in this area, and even `nio` and `nio2`
don't _truly_ help make file io code have a high degree of clarity.

Sample output is the same as shown in the challenge repository's readme.

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
> David Drummond for answering questions and making the challenge  
> the [Guava][guava] maintainers for such a breadth of performant time-savers  
> the [Dagger][dagger] maintainers for making DI less error-prone  
> the JCommander maintainers for a flexible argument parser  
> the Gradle maintainers for their fine minimally-full-featured build-package-deploy tool-set  
> JetBrains Community Edition contributors for the slickest IDE this side of a text editor  
> Perl 5 maintainers for keeping the tool, that can do these tasks in 1 to 40 lines, fast as ever  
> Git and GitHub for complementing each other and focusing on what you each do best  

###### - Daniel Lamblin ######

[challenge]: https://github.com/InsightDataScience/cc-example/tree/0d01fc8f703930ce522536230a3829d618f9fe99 "Insight Data Science Coding Challenge Example"
[challenge-july]: https://github.com/InsightDataScience/cc-example/tree/1eb0b6e398c0ad069436e65f90dc6285c319acc1 "Insight Data Science Coding Challenge Example"
[faq]: https://github.com/InsightDataScience/cc-example#faq "Challenge FAQ"
[oneliner]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/perl/non-scalable_10min_oneliner.pl
[Perl]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/perl/tweetStats.pl "Perl running median unique words per line and word count"
[counter]: https://docs.python.org/3.4/library/collections.html#collections.Counter "A Counter is a dictionary where key counts are stored as the key values"
[dagger]: http://square.github.io/dagger/ "Dagger"
[guava]: https://github.com/google/guava "com.google.common"
[javastyle]: https://google-styleguide.googlecode.com/svn/trunk/javaguide.html "Google Java Style"
[run]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/run.sh "run.sh"
[shellstyle]: https://google-styleguide.googlecode.com/svn/trunk/shell.xml "Google shell style guide"
[javadoc]: http://dlamblin.github.io/insight-data-engineering-code-challenge/javadoc/index.html "JavaDoc"
