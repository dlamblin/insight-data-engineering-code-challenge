package main

import (
	"bufio"
	"flag"
	"fmt"
	"insight-data-engineering-code-challenge/src/golang/cleaner"
	"io"
	"os"
	"sort"
	"strings"
)

// Breaks up input into lines and lines into words. Counts words in a map.
func main() {
	inputFilePtr := flag.String("i", "", "The file or read in if not stdin.")
	stopWordsFilePtr := flag.String("s", "", "The file with stopwords.")

	flag.Parse()

	// Set up the stop words from the path if given as an argument.
	if f := openIfNot(stopWordsFilePtr, ""); f != nil {
		defer f.Close()
		cleaner.SetStopWordsFromReader(bufio.NewReader(f))
	}

	// Set up the input either from a file or stdin.
	// TODO(lamblin) identify when a given path is a dir and read all its files
	var reader *bufio.Reader
	if f := openIfNot(inputFilePtr, ""); f != nil {
		defer f.Close()
		reader = bufio.NewReader(f)
	} else {
		reader = bufio.NewReader(os.Stdin)
	}

	m := readAndCount(reader)
	outputCounts(m)
}

// openIfNot opens the path of a flag string if it is not the default,
// returns the os.File pointer or nil if the flag was the default.
func openIfNot(stringFlag *string, isNot string) *os.File {
	if *stringFlag != isNot {
		f, err := os.Open(*stringFlag)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Could not open file: %q, reason: %q",
				*stringFlag, err)
			panic(err)
		}
		return f
	}
	return nil
}

// readAndCount reads the reader given and counts the words into a map which
// is returned.
func readAndCount(reader *bufio.Reader) map[string]int64 {
	m := make(map[string]int64)

	for {
		// Read lines.
		line, err := reader.ReadString('\n')
		if err != nil {
			if err != io.EOF {
				fmt.Fprintf(os.Stderr, "in wordcount main, error reading: %v\n", err)
			}
			break
		}
		// Break line into words and count them in the map.
		words := strings.Fields(line)
		for _, word := range words {
			if cleanWord, err := cleaner.Clean(word); err == nil {
				m[cleanWord]++
			}
		}
	}
	return m
}

// outputCounts outputs the words counted in sorted alphabetical order.
func outputCounts(m map[string]int64) {
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
		fmt.Printf("%-15v %v\n", word, m[word])
	}
}
