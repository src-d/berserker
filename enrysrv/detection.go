package enrysrv

import "gopkg.in/src-d/enry.v1"

type strategy struct {
	Name string
	Run  enry.Strategy
}

const (
	byExtension  = "EXTENSION"
	byFilename   = "FILENAME"
	byShebang    = "SHEBANG"
	byModeline   = "MODELINE"
	byContent    = "CONTENT"
	byClassifier = "CLASSIFIER"

	binary = "Binary"
)

var (
	strategiesByFilename = []*strategy{
		&strategy{Name: byExtension, Run: enry.GetLanguagesByExtension},
		&strategy{Name: byFilename, Run: enry.GetLanguagesByFilename},
	}

	strategiesByContent = []*strategy{
		&strategy{Name: byShebang, Run: enry.GetLanguagesByShebang},
		&strategy{Name: byModeline, Run: enry.GetLanguagesByModeline},
		&strategy{Name: byContent, Run: enry.GetLanguagesByContent},
		&strategy{Name: byClassifier, Run: enry.GetLanguagesByClassifier},
	}
)

func getLanguagesByFilenameStrategies(filename string) ([]string, string) {
	return runStrategies(strategiesByFilename, filename, nil, []string{})
}

func getLanguagesByContentStrategies(filename string, content []byte, candidates []string) ([]string, string) {
	if enry.IsBinary(content) {
		return []string{binary}, binary
	}

	return runStrategies(strategiesByContent, filename, content, candidates)
}

func runStrategies(strategies []*strategy, filename string, content []byte, candidates []string) (languages []string, strategyName string) {
	for _, s := range strategies {
		languages = s.Run(filename, content, candidates)
		if len(languages) == 1 {
			strategyName = s.Name
			return
		}

		if len(languages) > 0 {
			candidates = append(candidates, languages...)
		}
	}

	return
}

func isIgnored(filename string) bool {
	return enry.IsVendor(filename) || enry.IsDotFile(filename) ||
		enry.IsDocumentation(filename) || enry.IsConfiguration(filename)
}
