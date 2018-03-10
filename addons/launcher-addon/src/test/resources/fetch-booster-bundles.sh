#!/usr/bin/env bash

TEST_DIR="${PWD}/repos/boosters"
rm -rf ${TEST_DIR}
mkdir -p ${TEST_DIR} > /dev/null 2>&1
BOOSTER_TMP_DIR="${TEST_DIR}/booster-catalog"
git clone git@github.com:fabric8-launcher/launcher-booster-catalog.git ${BOOSTER_TMP_DIR}
cd ${BOOSTER_TMP_DIR}

git bundle create booster-catalog.bundle --all
cp booster-catalog.bundle ${TEST_DIR}

for repo in $(tail -n +1 $(find . -type f -name 'booster.yaml') | grep url\: | sed -e 's/^[ \t]*//' | cut -f2 -d' '); do
    cd ${BOOSTER_TMP_DIR}
    mkdir -p repos
    cd repos
    git clone $repo
    REPO_NAME=$(echo $repo | cut -f5 -d'/')
    cd $REPO_NAME
    git bundle create ${REPO_NAME}.bundle --all
    cp ${REPO_NAME}.bundle ${TEST_DIR}
done

cd ${TEST_DIR}

rm -rf ${BOOSTER_TMP_DIR}
