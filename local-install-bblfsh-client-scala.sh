#!/bin/bash

# Workaround to get Scala client for Bblfsh in CI
# Should be removed when org.bblfsh:bblfsh-client is published on oss.sonatype.org

alreadyInstalled="$HOME/.ivy2/local/org.bblfsh/"
bblfshClientScala="https://github.com/bzz/client-scala.git"

if [[ -d "${alreadyInstalled}" ]]; then
    echo "Found local version in '${alreadyInstalled}' skipping installation"
    exit 0
fi

git clone "${bblfshClientScala}" client-scala
if [[ "$?" -ne 0 ]]; then
  echo "Failed to clone ${bblfshClientScala}"
  exit 1
fi

cd client-scala
./sbt publish-local
cd ..