package enrysrv

import (
	enry "gopkg.in/src-d/enry.v1"
)

var (
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

func getLanguagesByFilenameStrategies(filename string) (languages []string) {
	if enry.IsVendor(filename) || enry.IsDotFile(filename) ||
		enry.IsDocumentation(filename) || enry.IsConfiguration(filename) {
		return
	}

	return runStrategies(strategiesByFilename, filename, nil, []string{})
}

func getLanguagesByContentStrategies(filename string, content []byte, candidates []string) (languages []string) {
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
