#!/usr/bin/env bash

TEST_DIR="${PWD}/repos/boosters"
rm -rf ${TEST_DIR}
mkdir -p ${TEST_DIR} > /dev/null 2>&1
BOOSTER_TMP_DIR="${TEST_DIR}/booster-catalog"
git clone git@github.com:openshiftio/booster-catalog.git ${BOOSTER_TMP_DIR}
cd ${BOOSTER_TMP_DIR}
for branch in `git branch -a | grep remotes | grep -v HEAD | grep -v master `; do
   git branch --track ${branch#remotes/origin/} $branch
done

git bundle create booster-catalog.bundle --all
cp booster-catalog.bundle ${TEST_DIR}

for repo in $(cat $(find . -type f -name '*.yaml') | grep githubRepo | cut -f2 -d' '); do
    cd ${BOOSTER_TMP_DIR}
    mkdir -p repos
    cd repos
    git clone https://github.com/$repo
    REPO_NAME=$(echo $repo | cut -f2 -d'/')
    cd $REPO_NAME
    git bundle create ${REPO_NAME}.bundle --all
    cp ${REPO_NAME}.bundle ${TEST_DIR}
done

cd ${TEST_DIR}

rm -rf ${BOOSTER_TMP_DIR}
