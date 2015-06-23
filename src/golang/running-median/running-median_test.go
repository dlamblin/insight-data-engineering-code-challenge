package main

import (
	"bufio"
	"io"
	"strings"
	"testing"
)

func TestCountWordsEachLine(t *testing.T) {
	// Setup some test cases with inputs and expected results.
	cases := []struct {
		input  string
		expect int64
		err    error
	}{
		{"", 0, io.EOF},
		{"000\n", 0, nil},
		{"a\n", 1, nil},
		{"a b \n b 5 a 1\n", 2, nil},
		{"a b c c 100.00\n", 4, nil},
	}

	for _, c := range cases {
		output, e := countWordsEachLine(bufio.NewReader(strings.NewReader(c.input)))
		if output != c.expect || e != c.err {
			t.Errorf("countWordsEachLine(%q)==%d words with %v, "+
				"expected %d words with %v", c.input, output, e, c.expect, c.err)
		}
	}
}

func TestCountWordsInLine(t *testing.T) {
	// Setup some test cases with inputs and expected results.
	cases := []struct {
		input  string
		expect int64
	}{
		{"", 0},
		{"000", 0},
		{"a", 1},
		{"a b ", 2},
		{" a b c c 100.00 ", 4},
	}

	for _, c := range cases {
		output := countWordsInLine(c.input)
		if output != c.expect {
			t.Errorf("countWordsInLine(%q) counted %d words expected %d words",
				c.input, output, c.expect)
		}
	}
}

func TestFindMedianInHistogram(t *testing.T) {
	// Setup some test cases with inputs and expected results.
	cases := []struct {
		inputHgram []int64
		inputLines int64
		expect     float64
	}{
		{[]int64{0, 0, 0, 0}, 0, 0},
		{[]int64{0, 2, 0, 0}, 0, 0},
		{[]int64{0, 0, 1, 0}, 1, 2.0},
		{[]int64{0, 1, 1, 0}, 2, 1.5},
		{[]int64{0, 3, 1, 0}, 4, 1.0},
		{[]int64{1, 1, 1, 2}, 5, 2.0},
		{[]int64{0, 2, 0, 0}, 6, 0}, // TODO(lamblin): resolve issues
	}

	for _, c := range cases {
		output := findMedianInHistogram(c.inputHgram, c.inputLines)
		if output != c.expect {
			t.Errorf("findMedianInHistogram(%v, %v)==%f, expected %f",
				c.inputHgram, c.inputLines, output, c.expect)
		}
	}
}
