package cleaner

import (
	"errors"
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

var nonAlphas = regexp.MustCompile(`\W+`)
var allDigits = regexp.MustCompile(`^\d+$`)

// Clean cleans a word to contain only alpha numerics and to not be all digits.
// TODO(lamblin) bring in a stopwords file.
func Clean(s string) (string, error) {
	r := nonAlphas.ReplaceAllLiteralString(strings.ToLower(s), "")
	if r == "" {
		return string(r), ErrAllNonAlphanums
	}
	if allDigits.MatchString(r) {
		return string(r), ErrAllDigits
	}
	return string(r), nil
}
