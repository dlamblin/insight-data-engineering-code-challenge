package cleaner

import (
	"bufio"
	"errors"
	"fmt"
	"io"
	"os"
	"regexp"
	"strings"
)

// The word that was cleaned contained only non-Alphanumerics thus it was not a
// word
var ErrAllNonAlphanums = errors.New(
	"Not a word, but all non-Alphanumerics")

// The word that was cleaned contained only digits and non-Alphanumerics thus it
// was not a word.
var ErrAllDigits = errors.New(
	"Not a word, but all digits and non-Alphanumerics")

// The word that was cleaned is a stop word thus it was not a word.
var ErrStopWord = errors.New("Not a word, in the stop words list")

var nonAlphas = regexp.MustCompile(`\W+`)
var allDigits = regexp.MustCompile(`^\d+$`)

var stopWords map[string]int

// Clean cleans a word to contain only alpha numerics and to not be all digits.
func Clean(s string) (string, error) {
	r := nonAlphas.ReplaceAllLiteralString(strings.ToLower(s), "")
	if r == "" {
		return string(r), ErrAllNonAlphanums
	}
	if allDigits.MatchString(r) {
		return string(r), ErrAllDigits
	}
	if stopWords != nil {
		if _, stop := stopWords[r]; stop {
			return string(r), ErrStopWord
		}
	}
	return string(r), nil
}

// SetStopWordsFromReader takes an bufio.Reader to read words from which will
// be considered stop words. IE words that should not be treated as valid words.
func SetStopWordsFromReader(r *bufio.Reader) error {
	stopWords = make(map[string]int)
	for {
		// Read each line as a full stop word.
		word, err := r.ReadString('\n')
		if err != nil {
			if err != io.EOF {
				fmt.Fprintf(os.Stderr,
					"in cleaner SetStopWordsFromReader error reading: %v\n", err)
				return err
			}
			break
		}
		// Trim the newline from the word and add it as a stop word.
		stopWords[strings.TrimSpace(word)]++
	}
	return nil
}
