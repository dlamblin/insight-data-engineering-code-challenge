package file

import (
	"fmt"
	"os"
	"path/filepath"
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

// CreateInDir will create a file in a diretory given by a flag string.
// returns the os.File pointer or exits or panics in case of issues.
func CreateInDir(stringFlag *string, filename string) *os.File {
	d, err := os.Stat(*stringFlag)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Could not open directory: %q, reason %q",
			*stringFlag, err)
		panic(err)
	}
	if !d.IsDir() {
		fmt.Printf("Specified output path %q is not a directory", *stringFlag)
		os.Exit(1)
	}
	targetFile := filepath.Join(*stringFlag, filename)
	f, err := os.Create(targetFile)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Could not create file: %q, reason %q",
			targetFile, err)
		panic(err)
	}
	return f
}
