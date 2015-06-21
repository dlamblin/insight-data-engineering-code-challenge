package cleaner

import "testing"

func TestClean(t *testing.T) {
	cases := []struct {
		input, expected string
	}{
		{"Hello", "hello"},
		{"F4T417Y", "f4t417y"},
		{"   ", ""},
		{"trailingspace ", "trailingspace"},
		{" leadingspace", "leadingspace"},
		{"1234567890", ""},
		{"1000.01", ""},
		{"10,000", ""},
	}
	for _, c := range cases {
		output := Clean(c.input)
		if output != c.expected {
			t.Errorf("Clean(%q) == %q, expected %q", c.input, output, c.expected)
		}
	}
}
