#!/usr/bin/env bash

#################################################
# Here you can find basic setup for this script #
#################################################

if [ -z "$KEYCLOAK" ]; then

    # Default KeyCloak will be used if KEYCLOAK variable is not set before.
    # Choose (uncomment) one of the 3 KeyCloak options below.

    KEYCLOAK=NO
    #KEYCLOAK=OFFICIAL
    #KEYCLOAK=LOCAL

fi

if [ -z "$OSIO" ]; then

    # Choose (uncomment) one of the OSIO options below.

    OSIO=STAGING
    #OSIO=PRODUCTION

fi

if [ -z "$ECHO_ENV" ]; then

    # Display environment vars?
    ECHO_ENV=YES
    #ECHO_ENV=NO

fi


#################################################
#################################################

echo ----- Environment is using $KEYCLOAK KeyCloak -----

SCRIPT_DIR=$(cd "$(dirname "$BASH_SOURCE")" ; pwd -P)

#### DO NOT CHANGE - Reset environment variables
unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL
unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL
unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME
unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD
unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN
unset LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME
unset LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN
unset LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME
unset LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN
unset LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME
unset LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD
unset LAUNCHER_KEYCLOAK_URL
unset LAUNCHER_KEYCLOAK_REALM
unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE
unset F8A_ANALYTICS_RECOMMENDER_API_URL
####

case "$KEYCLOAK" in
"NO")
    # No KeyCloak
    MSHIFT=$(minishift console --url)
    if [[ $MSHIFT != "https://"* ]]; then
        echo "ERROR: MiniShift is NOT running, the environment variables will NOT be properly set!"
        return
    fi
    export LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL=$MSHIFT
    export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=$MSHIFT
    export LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME=developer
    export LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD=developer
    # If set, will override username/password authentication scheme
        #export LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN=<token here>

    # Setup git providers default credentials (please look at README.md)
    export LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME=`git config github.user`
    export LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN=`git config github.token`
    export LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME=`git config gitlab.user`
    export LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN=`git config gitlab.token`
    export LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME=`git config bitbucket.user`
    export LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD=`git config bitbucket.password`
    ;;
"OFFICIAL")
    # Official KeyCloak
    export LAUNCHER_KEYCLOAK_URL=https://sso.openshift.io/auth
    export LAUNCHER_KEYCLOAK_REALM=rh-developers-launch
    export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE=$SCRIPT_DIR/clusters.yaml
    ;;
"LOCAL")
    # Local KeyCloak
    export LAUNCHER_KEYCLOAK_URL=http://localhost:8280/auth
    export LAUNCHER_KEYCLOAK_REALM=launch
    export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE=$SCRIPT_DIR/clusters.yaml
    ;;
*)
    echo ERROR: Failed to setup environment. Please choose a KEYCLOAK mode.
    [ $PS1 ] && return || exit;
    ;;
esac

# This will be set to "staging" on a staging server and "production" on a production server
  #export LAUNCHER_BACKEND_ENVIRONMENT=development
# This will prevent boosters being downloaded at startup making development faster (default = true)
  export LAUNCHER_PREFETCH_BOOSTERS=false

# For launchpad-booster-catalog-service
  #export LAUNCHER_BOOSTER_CATALOG_REPOSITORY=https://github.com/fabric8-launcher/launcher-booster-catalog.git
  #export LAUNCHER_BOOSTER_CATALOG_REF=master

# This can be used to filter boosters depending on their properties
# Eg. `booster.mission.id == 'rest-http'`
  #export LAUNCHER_BOOSTER_CATALOG_FILTER=

# For launchpad-frontend
  export LAUNCHER_MISSIONCONTROL_URL="ws://127.0.0.1:8080" #TODO needs to be default in the code (front-end)
  export LAUNCHER_BACKEND_URL="http://127.0.0.1:8080/api" #TODO needs to be default in the code (front-end)

# Testing tracker token
  export LAUNCHER_TRACKER_SEGMENT_TOKEN=dMV5AjaweCpO3KZop7TuZ0961UO74AF0

case "$OSIO" in
"STAGING")
    # For OSIO addon in the backend - Staging
    export WIT_URL=https://api.prod-preview.openshift.io
    export AUTH_URL=https://auth.prod-preview.openshift.io
    export KEYCLOAK_SAAS_URL=https://sso.prod-preview.openshift.io/
    export OPENSHIFT_API_URL=https://f8osoproxy-test-dsaas-preview.b6ff.rh-idev.openshiftapps.com
    export JENKINS_URL=https://jenkins.prod-preview.openshift.io
    export F8A_ANALYTICS_RECOMMENDER_API_URL=https://recommender.api.prod-preview.openshift.io
    ;;
"PRODUCTION")
    # OSIO - Production
    export WIT_URL=https://api.openshift.io
    export AUTH_URL=https://auth.openshift.io
    export KEYCLOAK_SAAS_URL=https://sso.openshift.io/
    export OPENSHIFT_API_URL=https://f8osoproxy-test-dsaas-production.09b5.dsaas.openshiftapps.com
    export JENKINS_URL=https://jenkins.openshift.io
    export F8A_ANALYTICS_RECOMMENDER_API_URL=https://recommender.api.openshift.io
    ;;
esac

# For OSIO frontend
  export FABRIC8_FORGE_API_URL=http://localhost:8080

# For Integration Tests
  # Generate an automated test osio offline token using instructions:
  # https://fabric8-services.github.io/fabric8-auth/reference.html#_automated_tests
  #export LAUNCHER_OSIO_TOKEN=<osio offline token>

case "$ECHO_ENV" in
"YES")
    # Display LAUNCHER environment
    env | grep 'LAUNCHER\|WIT_URL\|FABRIC8_FORGE_API_URL\|WIT_URL\|AUTH_URL\|KEYCLOAK_SAAS_URL\|OPENSHIFT_API_URL\|JENKINS_URL'
    ;;
esac


