#!/usr/bin/env bash

. inherit-env

yum -y update
yum -y install centos-release-scl java-1.8.0-openjdk-devel git
yum -y install rh-maven33

# TODO: Add token from Jenkins secret
export LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN=ADD_TOKEN_HERE

export LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL=https://dev.rdu2c.fabric8.io:8443/
export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=https://dev.rdu2c.fabric8.io:8443/
#export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE=<path to an openshift-clusters.yaml file>

export LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN=hoverfly
export LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN=hoverfly

export LAUNCHER_MISSIONCONTROL_SERVICE_HOST=localhost
export LAUNCHER_MISSIONCONTROL_SERVICE_PORT=8080
export LAUNCHER_KEYCLOAK_URL=https://sso.openshift.io/auth
export LAUNCHER_KEYCLOAK_REALM=rh-developers-launch
export LAUNCHER_BOOSTER_CATALOG_REPOSITORY=https://github.com/fabric8-launcher/launcher-booster-catalog.git
export LAUNCHER_BOOSTER_CATALOG_REF=master
export LAUNCHER_MISSIONCONTROL_URL=ws://localhost:8080/
export LAUNCHER_BACKEND_URL=http://localhost:8080/api/
export LAUNCHER_TESTS_TRUSTSTORE_PATH=${PWD}/services/git-service-impl/src/test/resources/hoverfly/hoverfly.jks
export TZ=GMT

# OSIO env vars
export WIT_URL=https://api.prod-preview.openshift.io
export AUTH_URL=https://auth.prod-preview.openshift.io
export KEYCLOAK_SAAS_URL=https://sso.prod-preview.openshift.io/
export OPENSHIFT_API_URL=https://f8osoproxy-test-dsaas-preview.b6ff.rh-idev.openshiftapps.com

scl enable rh-maven33 'mvn integration-test -Pit'

if [ $? -ne 0 ]; then
    echo 'Build Failed!'
    exit 1
fi