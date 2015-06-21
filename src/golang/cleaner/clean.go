package cleaner

import (
	"regexp"
	"strings"
)

var nonAlphas = regexp.MustCompile(`\W+`)
var allDigits = regexp.MustCompile(`^\d+$`)

// Clean cleans a word to contain only alpha numerics and not be all digits.
// TODO(lamblin) bring in a stopwords file.
func Clean(s string) string {
	r := nonAlphas.ReplaceAllLiteralString(strings.ToLower(s), "")
	if allDigits.MatchString(r) {
		return ""
	}
	return string(r)
}
