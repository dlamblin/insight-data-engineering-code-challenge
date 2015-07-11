package file

import "testing"

func TestOpenIfNot(t *testing.T) {
	// Setup some test cases with inputs and expected results.
	cases := []struct {
		input1    string
		input2    string
		expectNil bool
	}{
		{"file.go", ".", false},
		{".", ".", true},
		{".", "file.go", false},
	}

	for _, c := range cases {
		output := OpenIfNot(&c.input1, c.input2)
		if output != nil {
			defer output.Close()
			if c.expectNil {
				t.Errorf("openIfNot(%q, %q) == \"valid *os.File\"\n\tExpected \"nil\"",
					c.input1, c.input2)
			}
		} else {
			if !c.expectNil {
				t.Errorf("openIfNot(%q, %q) == \"nil\"\n\tExpected \"valid *os.File\"",
					c.input1, c.input2)
			}
		}
	}
}

func TestCreateInDir(t *testing.T) {
	// Setup some test cases with inputs and expected results.
	// TODO(lamblin): not really thorough enough.
	cases := []struct {
		input1    string
		input2    string
		expectNil bool
	}{
		{"/tmp", "test"},
	}

	for _, c := range cases {
		output := CreateInDir(&c.input1, c.input2)
		defer output.Close()
	}
}
