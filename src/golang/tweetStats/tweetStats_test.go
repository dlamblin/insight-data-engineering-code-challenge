package main

import (
	"bufio"
	"bytes"
	"strings"
	"testing"
)

func TestIngestSequenceDistribute(t *testing.T) {
	cases := []struct {
		input  string
		expect []string
	}{
		{"", []string{}},
		{"a\n", []string{"a\n"}},
		{"a\nb b\nc c c\nd\n\n", []string{"a\n", "b b\n", "c c c\n", "d\n", "\n"}},
	}

	for _, c := range cases {
		msgs := make(chan sequencedMessage, 5)
		// Write into the channel from the test case.
		go ingestSequenceDistribute(bufio.NewReader(strings.NewReader(c.input)), msgs)

		// Prep to read the results written to the channel in a go routine.
		i := 0
		for m := range msgs {
			exp := c.expect[i]
			if m.sequence != i || m.message != exp {
				t.Errorf("ingestSequenceDistribute(%q) produced %q, expected %q",
					c.input, m, sequencedMessage{i, exp})
			}
			i++
		}
		if len(c.expect) > i {
			t.Errorf("%v %v ingestSequenceDistribute(%q) produced less messages than "+
				"expected, missing: %q", len(c.expect), i, c.input, sequencedMessage{i, c.expect[i]})
		}
	}
}

func TestWorker(t *testing.T) {
	cases := []struct {
		input  []string
		expwc  []map[string]int
		expmed []uniqueCount
	}{
		{[]string{},
			[]map[string]int{},
			[]uniqueCount{}},
		{[]string{"a\n"},
			[]map[string]int{map[string]int{"a": 1}},
			[]uniqueCount{uniqueCount{0, 1}}},
		{[]string{"a\n", "a b\n", "c c c\n", "d\n", "\n"},
			[]map[string]int{
				map[string]int{"a": 1},
				map[string]int{"a": 1, "b": 1},
				map[string]int{"c": 3},
				map[string]int{"d": 1},
				map[string]int{}},
			[]uniqueCount{
				uniqueCount{0, 1},
				uniqueCount{1, 2},
				uniqueCount{2, 1},
				uniqueCount{3, 1},
				uniqueCount{4, 0}}},
	}

	for _, c := range cases {
		msgs := make(chan sequencedMessage, 5)
		wordc := make(chan map[string]int, 5)
		median := make(chan uniqueCount, 5)
		done := make(chan bool, 1)
		// Fill msgs channel
		for i, m := range c.input {
			msgs <- sequencedMessage{i, m}
		}
		close(msgs)
		go worker(msgs, wordc, median, done)
		<-done
		close(wordc)
		close(median)
		close(done)
		// Check if worker put the right wordc channel contents
		i := 0
		for wc := range wordc {
			if i < len(c.expwc) {
				e := c.expwc[i]
				stop := false
				for k, v := range e {
					if wc[k] != v {
						t.Errorf("Wordcount %q didn't contain expected values %q", wc, e)
						stop = true
						break
					}
				}
				if stop {
					break
				}
				for k, v := range wc {
					if e[k] != v {
						t.Errorf("Wordcount %q didn't contain expected values %q", wc, e)
						break
					}
				}
			} else {
				t.Errorf("More wordcount messages than expected. Got %q", wc)
			}
			i++
		}
		// Check if worker put the right median channel contents
		i = 0
		for med := range median {
			if i < len(c.expmed) {
				e := c.expmed[i]
				if e.sequence != med.sequence || e.count != med.count {
					t.Errorf("Median recieved %q didn't contain expected values %q",
						med, e)
					break
				}
			} else {
				t.Errorf("More median messages than expected. Got %q", med)
			}
			i++
		}
	}
}

func TestAccumulateCounts(t *testing.T) {
	// Setup some test cases with inputs and expected results.
	cases := []struct {
		input  []map[string]int
		expect string
	}{
		{[]map[string]int{map[string]int{"a": 1}},
			"a                           1\n"},
		{[]map[string]int{map[string]int{"a": 1}, map[string]int{}},
			"a                           1\n"},
		{[]map[string]int{map[string]int{"a": 1}, map[string]int{"a": 1}},
			"a                           2\n"},
		{[]map[string]int{map[string]int{"a": 2}, map[string]int{"a": 1, "b": 2}},
			"a                           3\nb                           2\n"},
	}

	for _, c := range cases {
		wordc := make(chan map[string]int, 5)
		done := make(chan bool, 1)
		// load wordc channel with input
		for _, m := range c.input {
			wordc <- m
		}
		close(wordc)

		// setup capture of output and start the accumulateCounts on the channel
		var b bytes.Buffer
		ft1 := bufio.NewWriter(&b)
		go accumulateCounts(wordc, ft1, done)
		// ensure we're done with the input
		<-done
		close(done)
		ft1.Flush()
		out := b.String()
		if out != c.expect {
			t.Errorf("accumulateCounts(%q, ...) =\n%v, expected\n%v",
				c.input, out, c.expect)
		}
	}
}

func TestCountWords(t *testing.T) {
	// Setup some test cases with inputs and expected results.
	cases := []struct {
		input  string
		expect map[string]int
	}{
		{"", map[string]int{}},
		{"000\n", map[string]int{"000": 1}},
		{"a\n", map[string]int{"a": 1}},
		{"@lav #an http://ow.ly/o8gt3 #an",
			map[string]int{"@lav": 1, "#an": 2, "http://ow.ly/o8gt3": 1}},
		{"a b \n b 5 a 1\n", map[string]int{"a": 2, "b": 2, "5": 1, "1": 1}},
		{"a b c c 100.00\n", map[string]int{"a": 1, "b": 1, "c": 2, "100.00": 1}},
	}

	for _, c := range cases {
		output := countWords(c.input)
		stop := false
		for k, v := range c.expect {
			if output[k] != v {
				t.Errorf("countWords(%q)==%d uniq words with %v, \n"+
					"expected %d uniq words with %v",
					c.input, len(output), output, len(c.expect), c.expect)
				stop = true
				break
			}
		}
		if stop {
			break
		}
		for k, v := range output {
			if c.expect[k] != v {
				t.Errorf("countWords(%q)==%d uniq words with %v, "+
					"expected %d uniq words with %v",
					c.input, len(output), output, len(c.expect), c.expect)
				break
			}
		}
	}
}

func TestFindMedianInHistogram(t *testing.T) {
	// Setup some test cases with inputs and expected results.
	cases := []struct {
		inputHgram []int
		inputLines int
		expect     float64
	}{
		{[]int{0, 0, 0, 0}, 0, 0},
		{[]int{0, 2, 0, 0}, 0, 0},
		{[]int{0, 0, 1, 0}, 1, 2.0},
		{[]int{0, 1, 1, 0}, 2, 1.5},
		{[]int{0, 3, 1, 0}, 4, 1.0},
		{[]int{1, 1, 1, 2}, 5, 2.0},
		{[]int{0, 2, 0, 0}, 6, 0}, // TODO(lamblin): resolve issues
	}

	for _, c := range cases {
		output := findMedianInHistogram(c.inputHgram, c.inputLines)
		if output != c.expect {
			t.Errorf("findMedianInHistogram(%v, %v)==%f, expected %f",
				c.inputHgram, c.inputLines, output, c.expect)
		}
	}
}
