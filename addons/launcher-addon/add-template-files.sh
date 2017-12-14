#!/usr/bin/env bash

rm -rf target/tmpdir
mkdir -p target/tmpdir
cd target/tmpdir
git init
git remote add origin https://github.com/fabric8-launcher/launcher-documentation.git
git config core.sparseCheckout true
echo "docs/topics/readme" >> .git/info/sparse-checkout
git pull origin master
echo Copying readme files to $1
mv docs/topics $1
