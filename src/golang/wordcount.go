package main

import (
	"bufio"
	"fmt"
	"io"
	"os"
	"sort"
	"strings"

	"./cleaner"
)

// Breaks up input into lines and lines into words. Counts words in a map.
func main() {
	m := make(map[string]int64)
	reader := bufio.NewReader(os.Stdin)
	for {
		// Read lines.
		line, err := reader.ReadString('\n')
		if err != nil {
			if err != io.EOF {
				fmt.Fprintf(os.Stderr, "%v\n", err)
			}
			break
		}
		// Break line into words and count them in the map.
		words := strings.Fields(line)
		for _, word := range words {
			m[cleaner.Clean(word)]++
		}
	}
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
