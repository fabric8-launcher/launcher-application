#!/usr/bin/env bash

TEST_DIR="${PWD}/repos"
mkdir -p ${TEST_DIR} > /dev/null 2>&1
git clone git@github.com:fabric8io/fabric8-pipeline-library "${TEST_DIR}/pipeline"
cd "${TEST_DIR}/pipeline"
for branch in `git branch -a | grep remotes | grep -v HEAD | grep -v master `; do
   git branch --track ${branch#remotes/origin/} $branch
done

git bundle create fabric8-jenkinsfile-library.bundle --all
cp  fabric8-jenkinsfile-library.bundle ${TEST_DIR}
rm -rf "${TEST_DIR}/pipeline"