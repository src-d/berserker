#!/bin/bash

# Workaround to get Java version of siva
# Should be removed when tech.sourced:siva-java_2.11:0.1.0-SNAPSHOT is published

alreadyInstalled="$HOME/.ivy2/local/tech.sourced/siva-java_2.11"
repoUrl="https://github.com/bzz/siva-java.git"

if [[ -d "${alreadyInstalled}" ]]; then
    echo "Found local version in '${alreadyInstalled}' skipping installation"
    exit 0
fi

git clone "${repoUrl}" siva-java
if [[ "$?" -ne 0 ]]; then
  echo "Failed to clone ${repoUrl}"
  exit 1
fi

cd siva-java
git checkout feature/siva-unpacker
./sbt publish-local
./sbt publishM2
cd ..
