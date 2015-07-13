// Gathers tweet statistics on median unique words per tweet and counts of words
package main

import (
	"bufio"
	"flag"
	"fmt"
	"io"
	"os"
	"runtime"
	"sort"
	"strings"

	"github.com/dlamblin/insight-data-engineering-code-challenge/src/golang/util/file"
)

const maxWordsPerLine = 69
const histSize = maxWordsPerLine + 1

var workers = runtime.NumCPU()

type uniqueCount struct {
	sequence, count int
}

type sequencedMessage struct {
	sequence int
	message  string
}

func main() {
	inputFilePtr := flag.String("i", "", "The file to read in if not stdin.")
	outputDirPtr := flag.String("o", "tweet_output",
		"The directory to write ft[12].txt to.")

	flag.Parse()

	// Set up the input either from a file or stdin.
	// TODO(lamblin) identify when a given path is a dir and read all its files
	var reader *bufio.Reader
	if f := file.OpenIfNot(inputFilePtr, ""); f != nil {
		defer f.Close()
		reader = bufio.NewReader(f)
	} else {
		reader = bufio.NewReader(os.Stdin)
	}

	// Set up the output for the word counts file
	f1 := file.CreateInDir(outputDirPtr, "ft1.txt")
	ft1 := bufio.NewWriter(f1)
	defer ft1.Flush()
	defer f1.Close()

	// Set up the output for the running median file
	f2 := file.CreateInDir(outputDirPtr, "ft2.txt")
	ft2 := bufio.NewWriter(f2)
	defer ft2.Flush()
	defer f2.Close()

	messages := make(chan sequencedMessage, workers)
	wordc := make(chan map[string]int, workers)
	median := make(chan uniqueCount, workers)
	done := make(chan bool, workers)

	runtime.GOMAXPROCS(workers)
	for i := 0; i <= workers; i++ {
		go worker(messages, wordc, median, done)
	}
	go runningMedian(median, ft2, done)
	go accumulateCounts(wordc, ft1, done)

	// Send work to the workers
	ingestSequenceDistribute(reader, messages)

	// Wait for all the workers to be done
	for i := 0; i <= workers; i++ {
		<-done
	}
	close(wordc)
	close(median)
	// waiting for runningMedian & accumulateCounts
	<-done
	<-done
	close(done)
}

func ingestSequenceDistribute(r *bufio.Reader, messages chan sequencedMessage) {
	var s = 0
	for {
		if line, err := r.ReadString('\n'); err == nil {
			messages <- sequencedMessage{s, line}
			s++
		} else {
			if err != io.EOF {
				fmt.Fprintf(os.Stderr, "in ingest, error reading: %v\n", err)
			}
			break
		}
	}
	close(messages)
}

func worker(messages chan sequencedMessage,
	wordc chan map[string]int, median chan uniqueCount, done chan bool) {
	for s := range messages {
		count := countWords(s.message)
		median <- uniqueCount{s.sequence, len(count)}
		wordc <- count
	}
	done <- true
}

func countWords(line string) map[string]int {
	// Break line into words and count each valid word.
	count := make(map[string]int)
	for _, word := range strings.Fields(line) {
		if c, ok := count[word]; ok == true {
			count[word] = c + 1
		} else {
			count[word] = 1
		}
	}
	return count
}

// Read the unique counts, resequence, and output running median to a file
func runningMedian(median chan uniqueCount, ft2 *bufio.Writer, done chan bool) {
	var histogram []int
	var lines int
	buffer := make(map[int]int)
	expect := 0
	histogram = make([]int, histSize)
	for s := range median {
		buffer[s.sequence] = s.count
		for c, ok := buffer[expect]; ok == true; c, ok = buffer[expect] {
			delete(buffer, expect)
			expect++
			histogram[c]++
			lines++
			outputMedian(histogram, lines, ft2)
		}
	}
	done <- true
}

func outputMedian(histogram []int, totalLines int, ft2 *bufio.Writer) {
	s := fmt.Sprintf("%0.1f\n", findMedianInHistogram(histogram, totalLines))
	ft2.WriteString(s)
}

func findMedianInHistogram(hgram []int, totalLines int) float64 {
	if totalLines == 0 {
		return 0
	}
	var targets int8
	var target int
	var median float64
	if totalLines%2 == 0 {
		target = totalLines / 2
		targets = 2
	} else {
		target = (totalLines + 1) / 2
		targets = 1
	}

	for count, seen := range hgram {
		target -= seen
		if target <= 0 && seen > 0 {
			median += float64(count)
			targets--
			if targets <= 0 {
				break
			}
			if target < 0 {
				median += float64(count)
				break
			}
		}
	}
	if totalLines%2 == 0 {
		median /= 2.0
	}
	return median
}

// accumulateCounts combines the counts from each message into a total count.
func accumulateCounts(wordc chan map[string]int, ft1 *bufio.Writer,
	done chan bool) {
	totals := make(map[string]int)
	for c := range wordc {
		for k, v := range c {
			if c, ok := totals[k]; ok == true {
				totals[k] = c + v
			} else {
				totals[k] = v
			}
		}
	}
	outputCounts(totals, ft1)
	done <- true
}

// outputCounts outputs the words counted in sorted alphabetical order.
func outputCounts(m map[string]int, ft1 *bufio.Writer) {
	// Sort the words in the map.
	words := make([]string, len(m))
	var i int
	for word := range m {
		words[i] = word
		i++
	}
	sort.Strings(words)
	// Print the words in the map by the sorted words.
	for _, word := range words {
		s := fmt.Sprintf("%-27v %v\n", word, m[word])
		ft1.WriteString(s)
	}
}
