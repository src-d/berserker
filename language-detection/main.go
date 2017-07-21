package main

import (
	"fmt"
	"io/ioutil"
	"os"

	"gopkg.in/src-d/enry.v1"
)

const (
	needContent = "NEED_CONTENT"
)

var (
	input  = os.Stdin
	output = os.Stdout

	strategiesByFilename = []enry.Strategy{
		enry.GetLanguagesByExtension,
		enry.GetLanguagesByFilename,
	}

	strategiesByContent = []enry.Strategy{
		enry.GetLanguagesByShebang,
		enry.GetLanguagesByModeline,
		enry.GetLanguagesByContent,
		enry.GetLanguagesByClassifier,
	}
)

func main() {
	if len(os.Args) == 1 {
		os.Exit(1)
	}

	filename := os.Args[1]
	languages := GetLanguagesByFilenameStrategies(filename)
	if len(languages) == 1 {
		fmt.Fprint(output, languages[0])
		return
	}

	fmt.Print(needContent)
	content, err := ioutil.ReadAll(input)
	if err != nil {
		fmt.Fprint(output, err)
		os.Exit(1)
	}

	languages = GetLanguagesByContentStrategies(filename, content, languages)
	if len(languages) == 0 {
		return
	}

	fmt.Fprint(output, languages[0])
}

func GetLanguagesByFilenameStrategies(filename string) (languages []string) {
	if enry.IsVendor(filename) || enry.IsDotFile(filename) ||
		enry.IsDocumentation(filename) || enry.IsConfiguration(filename) {
		return
	}

	return runStrategies(strategiesByFilename, filename, nil, []string{})
}

func GetLanguagesByContentStrategies(filename string, content []byte, candidates []string) (languages []string) {
	if enry.IsBinary(content) {
		return
	}

	return runStrategies(strategiesByContent, filename, content, candidates)
}

func runStrategies(strategies []enry.Strategy, filename string, content []byte, candidates []string) (languages []string) {
	for _, strategy := range strategies {
		languages = strategy(filename, content, candidates)
		if len(languages) == 1 {
			return
		}

		if len(languages) > 0 {
			candidates = append(candidates, languages...)
		}
	}

	return
}
