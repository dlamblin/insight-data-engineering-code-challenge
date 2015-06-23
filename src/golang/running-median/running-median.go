package main

import (
	"bufio"
	"flag"
	"fmt"
	"insight-data-engineering-code-challenge/src/golang/util/cleaner"
	"insight-data-engineering-code-challenge/src/golang/util/file"
	"io"
	"os"
	"strings"
)

const maxWordsPerLine = 50
const histSize = maxWordsPerLine + 1

func main() {
	inputFilePtr := flag.String("i", "", "The file to read in if not stdin.")
	stopWordsFilePtr := flag.String("s", "", "The file with stopwords.")

	flag.Parse()

	// Set up the stop words from the path if given as an argument.
	if f := file.OpenIfNot(stopWordsFilePtr, ""); f != nil {
		defer f.Close()
		cleaner.SetStopWordsFromReader(bufio.NewReader(f))
	}

	// Set up the input either from a file or stdin.
	// TODO(lamblin) identify when a given path is a dir and read all its files
	var reader *bufio.Reader
	if f := file.OpenIfNot(inputFilePtr, ""); f != nil {
		defer f.Close()
		reader = bufio.NewReader(f)
	} else {
		reader = bufio.NewReader(os.Stdin)
	}

	runningMedian(reader)
}

func runningMedian(r *bufio.Reader) {
	var histogram []int64
	var lines int64
	histogram = make([]int64, histSize)
	for {
		if count, err := countWordsEachLine(r); err == nil {
			histogram[count]++
			lines++
			outputMedian(histogram, lines)
		} else {
			break
		}
	}
}

func countWordsEachLine(r *bufio.Reader) (int64, error) {
	line, err := r.ReadString('\n')
	if err != nil {
		if err != io.EOF {
			fmt.Fprintf(os.Stderr, "in running-median countWordsEachLine, "+
				"error reading: %v\n", err)
		}
		return 0, err
	}
	return countWordsInLine(line), nil
}

func countWordsInLine(line string) int64 {
	// Break line into words and count each valid word.
	words := strings.Fields(line)
	count := int64(0)
	for _, word := range words {
		if _, err := cleaner.Clean(word); err == nil {
			count++
		}
	}
	return count
}

func outputMedian(histogram []int64, totalLines int64) {
	fmt.Printf("%0.1f\n", findMedianInHistogram(histogram, totalLines))
}

func findMedianInHistogram(hgram []int64, totalLines int64) float64 {
	if totalLines == 0 {
		return 0
	}
	var targets int8
	var target int64
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
