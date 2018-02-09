#!/usr/bin/env bash

# Exit on error
set -e

KEYCLOAK=OFFICIAL

source launcher-env-template.sh

yum -y update
yum -y install centos-release-scl java-1.8.0-openjdk-devel git
yum -y install rh-maven33

scl enable rh-maven33 'mvn install failsafe:integration-test failsafe:verify -Pit -B'

if [ $? -ne 0 ]; then
    echo 'Build Failed!'
    exit 1
fi
