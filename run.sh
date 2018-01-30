#!/usr/bin/env bash

export LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME=`git config github.user`
export LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN=`git config github.token`
export LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME=`git config gitlab.user`
export LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN=`git config gitlab.token`
export LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL=`minishift console --url`
export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=`minishift console --url`
export LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME=developer
export LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD=developer
#export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE=<path to an openshift-clusters.yaml file>
export LAUNCHER_MISSIONCONTROL_SERVICE_HOST=localhost
export LAUNCHER_MISSIONCONTROL_SERVICE_PORT=8080
export LAUNCHER_KEYCLOAK_URL=https://sso.openshift.io/auth
export LAUNCHER_KEYCLOAK_REALM=rh-developers-launch
export LAUNCHER_BOOSTER_CATALOG_REPOSITORY=https://github.com/fabric8-launcher/launcher-booster-catalog.git
export LAUNCHER_BOOSTER_CATALOG_REF=openshift-online-free
export LAUNCHER_MISSIONCONTROL_URL=ws://localhost:8080/
export LAUNCHER_BACKEND_URL=http://localhost:8080/api/
export TZ=GMT

# OSIO env vars
export WIT_URL=https://api.prod-preview.openshift.io
export AUTH_URL=https://auth.prod-preview.openshift.io
export KEYCLOAK_SAAS_URL=https://sso.prod-preview.openshift.io/
export OPENSHIFT_API_URL=https://f8osoproxy-test-dsaas-preview.b6ff.rh-idev.openshiftapps.com

java -jar web/target/launcher-backend-swarm.jar