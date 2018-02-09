#!/usr/bin/env bash

. inherit-env

yum -y update
yum -y install centos-release-scl java-1.8.0-openjdk-devel git
yum -y install rh-maven33

scl enable rh-maven33 'mvn clean verify -B'

if [ $? -ne 0 ]; then
    echo 'Build Failed!'
    exit 1
fi