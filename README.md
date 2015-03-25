Insight Data Engineering Coding Challenge
=========================================

Implemented versions overview
-----------------------------

- [_One-liners_](#one-liners)
- [_Java_](#java)
- [_Perl_](#perl)

If you're unfamiliar with the challenge this project refers to,
please read this [quick overview](#challenge-overview-for-the-unfamiliar).

One-liners
----------
The one-liners [`non-scalable_10min_oneliner*.pl`][oneliner1] were the first
code I  produced to answer the challenge, in oneliner form, not a lot of time
was spent on them. They were updated after looking at the FAQ point on removing
numbers, and now  match output from the _Java_ program.
The [naive median approach][oneliner2]
over all turns out to be slow, because it's using an array, and sorting it
$O(n \log n)$ every time something is added, to give the median an $O(1)$
look-up performance after the sort. This is a total of so $O(n \log n)$ for
each line, growing with each line processed.

I wanted to show a quick implementation and basic solution, with the full
caveat that one-liners are not clean, well documented nor scalable.

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
unclear still allows for its use when theres a significant reduction in lines
thus aiding readability. It should be noted that the DirectoryStream claims
to be unordered but always behaved in alphabetic order on my system, prompting
me to drop circling back to sort the directory listing for the running median
in the interest of time.

### Other Notes ###
The style of the _Java_ code tries to hue to the
[Google Java Style Guide][javastyle].

The arguments, input and output options are more flexible than the challenge
specified, reading directories, files, or stdin, and writing to files or
stdout. It may not seem like much compared to features built in _Perl_ and
_Python_, but _Java_ is a little messy in this area, and even `nio` and `nio2`
don't _truely_ help make file io code have a high degree of clarity.

Perl
----
The _Perl_ version was a total rewrite, discarding the one-liner approach.
It was quick and easy to make and I hope the comments keep it readable.
This version's [running median command][PRM] mirrors the [`RangeRunningMedian`][RRM]
frequency counting approach, making finding the median a $O(1)$ operation
after a $O(n \log n)$ sort. It doesn't preallocate the size of the hash of
frequencies, so no range need be specified.

As noted in the source, I am certain that modules exist to bind the hash to
an implementation that retains its keys in sorted order, and that there also
exists a binding to disk backing. This would allowing the program to exit and
then pick up with the running median from before exiting. I'm unsure how this
would affect performance, but the sorted keys should speed up the overall
process by reducing the work to sort keys after each insert.

Shell script
------------
The support file, `run.sh`, has its own usage information, which shows when
invoked with `h` or `help`. It can run either the _oneliner_, _Java_, or _Perl_
version, and it can also clean the project and download a set of books from
Gutenberg.org.

Please do [read through the script][run]; it is also mostly in the
[Google shell style][shellstyle]. As per the challenge, it is intended as the primary
method of using the code in this project.

Sample output on sample.txt
---------------------------
Once the `run.sh java` script has finished with `./gradlew installApp`, you could
manually skip using `run.sh` and use the programs directly as in the below
demonstration:

    $ project='InsightDataEngineeringCodingChallenge'
    $ perl non-scalable_10min_oneliner2.pl sample.txt 
    5.0
    4.5
    4.0
    4.5
    $ ./build/install/$project/bin/runningMedianWordsPerLine -i sample.txt 
    5.0
    4.5
    4.0
    4.5
    $ ./src/main/perl/runningMedianWordsPerLine.pl sample.txt
    5.0
    4.5
    4.0
    4.5
    $ perl non-scalable_10min_oneliner1.pl sample.txt 
    a              	1
    big            	1
    call           	1
    every          	2
    everyone       	1
    get            	1
    holler         	1
    make           	2
    meeting        	1
    out            	2
    shout          	2
    so             	1
    who            	2
    $ ./build/install/$project/bin/wordCount -i sample.txt 
    a              	1
    big            	1
    call           	1
    every          	2
    everyone       	1
    get            	1
    holler         	1
    make           	2
    meeting        	1
    out            	2
    shout          	2
    so             	1
    who            	2
    $ ./src/main/perl/wordCount.pl sample.txt 
    a              	1
    big            	1
    call           	1
    every          	2
    everyone       	1
    get            	1
    holler         	1
    make           	2
    meeting        	1
    out            	2
    shout          	2
    so             	1
    who            	2
    $ ./build/install/$project/bin/wordCount -i sample.txt -s stop.txt 
    big            	1
    call           	1
    every          	2
    everyone       	1
    get            	1
    holler         	1
    make           	2
    meeting        	1
    out            	2
    shout          	2
    so             	1
    who            	2

Challenge Overview for the Unfamiliar
--------------------------------------
To learn what this challenge is about, please have a look at the
[challenge as posed][challenge] with [its FAQ][faq] etc.

Simply put, the goal is to count the words in some text files, and to calculate
the median number of words per line in the same files. Using some definition
for what is and is not a word that's specified in the FAQ. My short version is:

- Assume the files are just `ASCII`, not `UTF-8` or other encodings
- Split words on whitespace only.
- Clean out hyphens, punctuation, and all other non-AlphaNumerics (I read this
to not clean underscore).
- Do not store or count any empty strings or words consisting entirely of
digits (EG "10011")

I was given a 5 day timeframe to try this challenge. I think the challenge was
posed in batches, some starting 2-3 weeks earlier than me, but I assume everyone
overall had the same timeframe of about 5 days to finish as I had.

I do plan to continue updating this project as noted in the `run.sh`.

If you try out my code, in the output of words you will see results like these
"words" formed from URLs or HTML when following these rules above:

    httpsgithubcomdlamblininsightdataengineeringcodechallenge
    htmlheadtitle404

That means everything is working normally; there will be many more normal
words found across input files.

##### Thanks to: #####
> Anne Bessman and David Drummond for answering questions and making the challenge  
> the Guava maintainers for such a bredth of performant time-savers  
> the Dagger maintainers for making DI less error-prone  
> the JCommander maintainers for a flexible argument parser  
> the Gradle maintainers for their fine minimally-full-featured build-package-deploy toolset  
> JetBrains Community Edition contributors for the slickest IDE this side of a text editor  
> Perl 5 maintainers for keeping the tool, that can do these tasks in 1 to 40 lines, fast as ever  
> Git and GitHub for complementing each other and focusing on what you each do best  

###### - Daniel Lamblin ######

[challenge]: https://github.com/InsightDataScience/cc-example "Insight Data Science Coding Challenge Example"
[faq]: https://github.com/InsightDataScience/cc-example#faq "Challenge FAQ"
[oneliner1]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/non-scalable_10min_oneliner1.pl
[oneliner2]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/non-scalable_10min_oneliner2.pl
[RRM]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/main/java/lamblin/medianwordsperline/RangeRunningMedian.java "RangeRunningMedian"
[QRM]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/main/java/lamblin/medianwordsperline/QueueRunningMedian.java "QueueRunningMedian"
[WC]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/main/java/lamblin/common/source/word/filter/WordCleaner.java "WordCleaner"
[WA]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/main/java/lamblin/wordcount/WordAccumulator.java "WordAccumulator"
[WCT]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/main/java/lamblin/medianwordsperline/WordCountTransformer.java "WordCountTransformer"
[RMM]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/main/java/lamblin/medianwordsperline/RunningMedianModule.java "RunningMedianModule"
[PRM]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/src/main/perl/runningMedianWordsPerLine.pll "Perl running median words per line"
[dagger]: http://square.github.io/dagger/ "Dagger"
[guava]: https://github.com/google/guava "com.google.common"
[javastyle]: https://google-styleguide.googlecode.com/svn/trunk/javaguide.html "Google Java Style"
[run]: https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/run.sh "run.sh"
[shellstyle]: https://google-styleguide.googlecode.com/svn/trunk/shell.xml "Google shell style guide"
[javadoc]: http://dlamblin.github.io/insight-data-engineering-code-challenge/javadoc/index.html "JavaDoc"

