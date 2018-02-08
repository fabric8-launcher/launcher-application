#!/usr/bin/env bash

git clone https://github.com/fabric8io/fabric8-jenkinsfile-library.git --depth=1 $1
# Remove .git folder
rm -rf $1/.git