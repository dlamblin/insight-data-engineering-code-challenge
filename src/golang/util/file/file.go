package file

import (
	"fmt"
	"os"
)

// OpenIfNot opens the path of a flag string if it is not the default,
// returns the os.File pointer or nil if the flag was the default.
func OpenIfNot(stringFlag *string, isNot string) *os.File {
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
