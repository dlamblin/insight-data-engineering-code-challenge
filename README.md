Insight Data Engineering Coding Challenge
=========================================

If you don't know what the challenge is about, first, please have a look at the [challenge as posed](https://github.com/InsightDataScience/cc-example "Insight Data Science Coding Challenge Example") with [its FAQ](https://github.com/InsightDataScience/cc-example#faq "Challenge FAQ") and such. You probably also see a few solutions all over GitHub. I think they got started in batches. Some started 2 weeks earlier, but I assume they overall has the same timeframe of about 5 days
as I had. I do plan to continue updating this project as noted in the run.sh.

Simply put, the goal is to count the words in some text files, and to calculate the median number of words per line in the same files. Using some definition for what is and is not a word that's specified in the FAQ. My short version is:

- Assume the files are just `ASCII`, not `UTF-8` or other encodings
- Split words on whitespace only.
- Clean out hyphens, punctuation, and all other non-AlphaNumerics (I read this to not clean underscore).
- Do not store or count any empty strings or words consisting entirely of digits (EG "10011")

Overall the only really awkward thing about these programs is that, as per the
filtering advice, urls and any html tags get horribly mangled into messes
like these "words":

    httpsgithubcomdlamblininsightdataengineeringcodechallenge
    htmlheadtitle404

Implemented Versions:

- One-liners
- Java
- Perl

One-liners
----------
The one-liners [`non-scalable_10min_oneliner*.pl`](https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/non-scalable_10min_oneliner1.pl) were the first code I produced
to answer the challenge, they really only took 10-15 minutes to write. After
looking at the FAQ about removing numbers they were updated quickly to match
output from the java program. The [naive median approach](https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/non-scalable_10min_oneliner2.pl) of using an array,
and sorting it $O(n \log n)$ every time something is added, giving the median a
$O(1)$ look-up after the sort (so $O(n \log n)$ over all), turns out to be slow.

I just wanted to show the quickest to implement basic solution, with the full caveat that it
is not clean, well documented nor scalable.

Java
----
While I originally thought to move on to writing this in a couple of languages,
Python and Go sprang to mind. I wanted to commit to also adding in Java code.
Knowing that Java is verbose, and trying out two thing I was a bit new to:
Gradle and Dagger, I quickly sank all my time into this version. It is probably
a little over-engineered for the challenge.

I have not tuned any of the jvm properties to allocate more memory or tune
garbage collection. If the inputs were truly at scale, that would be
necessary.

### Trade-offs ###
The dependency injection allows me to swap out a range-limited frequency based
running median class with a dual-heap based approach for the same that is not limited in 
input range, but which is hindered by needing to store each word count for each
line. You can try it out with the `-u` option. See also `-h`.

The dependency injection might not be used idomatically, some cleanup of Dagger
modules seems in order as I learn more about Dagger. At least it's compile time
and doesn't cause a bunch of overhead at startup.

The word counting accumulator was implemented with a concurrent multiset. This
allows it to have a thread pool updating the multiset as words come in, and then
terminate when the set's results are requested. The accumulated maintains a 
separate sorted set of words so that the output can be shown in order.
This uses more memory overall. It works pretty quickly, but I had two other
versions I wanted to develop (I ran out of time for them):  

- using a tree multiset would maintain its key set in sorted order, using less
  memory probably than I do now. However, a quick test of dropping it in showed
  it really wasn't concurrent, so it remains to be seen if it would compete
  with the threaded version. There is a chance that for normal size corpuses
  the overhead of managing a threadpool is actually more significant than the
  savings of bringing more cores to bear on the work.
- The other option I wanted to try would be a trie based accumulator.  I had a
  trie class written during a phone interview but not on hand, and it would be
  very memory efficient, and also allow for alphabetic traversal (pre-order)
  easily for the output. With input and retrieval being $O(1)$ regarding the
  number of words stored or $O(n)$ regarding the length of the word being stored
  or retreived.

The verbosity of the input and output handling allow for some further development
and expansion of scope, not to mention optional median and word accumulators.
E.G. doing a running median of a different statistic per line than word count
would be a matter of swappig in a different transformation function in the
configuration module.

The Guava functional approach was used when reading directories. This allows the
directory readers to be composed of chained file readers, which in turn (with
standard input readers) are based on just the main logic in the reader handlers.
The warning in Guava about functional programming being unclear does allow for
it when it saves significant lines and remains readable. I think it does. It
Should be noted that the DirectoryStream claims to be unordered but always
behaved in alphabetic order on my system, leaving me to drop circling back to
sort the directory listing for median reading in the interest of time.

### Other Notes ###
The style of the Java code tries to hue to the [Google Java Style Guide](https://google-styleguide.googlecode.com/svn/trunk/javaguide.html "Google Java Style").

The arguments and input and output options are more flexible than the challenge
specified, reading directories, files, or stdin, and writing to files or stdout.
It doesn't seem like much compared to built in _Perl_ and _Python_ features, but
_Java_ is a little messy in this area, and I'm unclear on if `nio` or `nio2` _truely_
help.

Perl
----
The _Perl_ version was quick and easy to make. Even when doing a total rewrite
without a one-liner outlook. This version's running median command takes a
frequency counting approach, making finding the median a $O(1)$ operation after
a $O(n \log n)$ sort. It does not preallocate the size of the hash of frequencies.

As noted in the source, I am very certain that modules exist to bind the hash
to an implementation that retains its keys in sorted order, and that also a
binding to disk exists, allowing the program to exit, and then pick up with the
running median from disk. I haven't had time to hunt down the correct use of
these, but I believe they'd add maybe 3 lines, assuming the packages built and
installed. I'm unsure how the latter performs, but the former should speed up
the overall process.


Shell script
------------
The support file, `run.sh`, has it's own usage when invoked with h or help. It
can run either the java, perl, or oneliner version, and it can also clean and
download a set of books from the Gutenberg Project. Please do [read through the
script](https://github.com/dlamblin/insight-data-engineering-code-challenge/blob/master/run.sh); it is also mostly in the [Google shell style guide](https://google-styleguide.googlecode.com/svn/trunk/shell.xml "Google shell style guide") format. As per the challenge, it is intended as the primary method of using the code in this project.


Sample output on sample.txt
---------------------------
Once the `run.sh` script has issued `./gradlew installApp` you could manually skip using `run.sh` to use the programs as in the below demonstration:

    $ perl non-scalable_10min_oneliner2.pl sample.txt 
    5.0
    4.5
    4.0
    4.5
    $ ./build/install/InsightDataEngineeringCodingChallenge/bin/runningMedianWordsPerLine -i sample.txt 
    5.0
    4.5
    4.0
    4.5
    $ ./src/main/perl/runinngMedianWordsPerLine.pl sample.txt 
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
    $ ./build/install/InsightDataEngineeringCodingChallenge/bin/wordCount -i sample.txt 
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
    $ ./build/install/InsightDataEngineeringCodingChallenge/bin/wordCount -i sample.txt -s stop.txt 
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



