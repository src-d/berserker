package main

import (
	"flag"
	"fmt"
	"log"
	"os"
	"runtime/pprof"

	"github.com/src-d/berserker/extractor"
)

var cpuprofile = flag.String("cpuprofile", "", "write cpu profile to file")
var memprofile = flag.String("memprofile", "", "write memory profile to this file")
var n = flag.Uint64("n", 0, "number of repositories, 0 = All from DB")

func main() {
	flag.Parse()
	if *cpuprofile != "" {
		f, err := os.Create(*cpuprofile)
		if err != nil {
			log.Fatal(err)
		}
		pprof.StartCPUProfile(f)
		defer pprof.StopCPUProfile()
	}

	extractorService := extractor.NewService()
	repos, err := extractorService.GetRerpoData(*n)
	checkIfError(err)
	fmt.Printf("Repos returned: %d\n", len(repos))

	if *memprofile != "" {
		f, err := os.Create(*memprofile)
		if err != nil {
			log.Fatal(err)
		}
		pprof.WriteHeapProfile(f)
		f.Close()
		return
	}
}

func checkIfError(err error) {
	if err == nil {
		return
	}

	fmt.Printf("Runtime error: %+v", err)
	os.Exit(1)
}
