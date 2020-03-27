#!/usr/bin/env bash

#################################################
# Here you can find basic setup for this script #
#################################################

if [ -z "$LAUTH" ]; then

    # Default KeyCloak will be used if KEYCLOAK variable is not set before.
    # Choose (uncomment) one of the 3 KeyCloak options below.

    LAUTH=TOKEN
    #LAUTH=KEYCLOAK
    #LAUTH=OAUTH

fi

if [ -z "$ECHO_ENV" ]; then

    # Display environment vars?
    ECHO_ENV=YES
    #ECHO_ENV=NO

fi

if [ -z "$UNSET_ENV" ]; then

    # Unset environment vars?
    UNSET_ENV=YES
    #UNSET_ENV=NO

fi

#################################################
#################################################

echo ----- Environment is using $LAUTH authentication -----

SCRIPT_DIR=$(cd "$(dirname "$BASH_SOURCE")" ; pwd -P)

#### DO NOT CHANGE - Reset environment variables
case "$UNSET_ENV" in
"YES")
  unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL
  unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL
  unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME
  unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD
  unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN
  unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_IMPERSONATE_USER
  unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE
  unset LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME
  unset LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN
  unset LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN
  unset LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME
  unset LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME
  unset LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD
  unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_SUBSCRIPTION_TOKEN
  unset LAUNCHER_KEYCLOAK_URL
  unset LAUNCHER_KEYCLOAK_REALM
  unset LAUNCHER_KEYCLOAK_CLIENT_ID
  unset LAUNCHER_OAUTH_OPENSHIFT_URL
  unset LAUNCHER_GIT_PROVIDER
  unset LAUNCHER_GIT_PROVIDERS_FILE
  unset LAUNCHER_HOVERFLY_CAPTURE
    ;;
esac
####

case "$LAUTH" in
"TOKEN"|"OAUTH")
    # No KeyCloak
    OCAPI=$(oc whoami --show-server)
    OCCONSOLE=$(oc whoami --show-console)
    if [[ $OCAPI != "https://"*  || $OCCONSOLE != "https://"* ]]; then
        echo "WARNING: No active connection to OpenShift detected, these ENV vars must be set manually"
        echo "   LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL"
        echo "   LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL"
	return
    else
        export LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL=$OCAPI
        export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=$OCCONSOLE
    fi
esac

case "$LAUTH" in
"TOKEN")
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
"KEYCLOAK")
    # Official KeyCloak
    export LAUNCHER_KEYCLOAK_URL=https://sso.openshift.io/auth
    export LAUNCHER_KEYCLOAK_REALM=rh-developers-launch
    #export LAUNCHER_KEYCLOAK_CLIENT_ID=???
    export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE=$SCRIPT_DIR/clusters.yaml
    ;;
"OAUTH")
    # Necessary for OpenShift v4+, comment it out for v3
    # See https://docs.openshift.com/container-platform/4.1/authentication/configuring-internal-oauth.html#oauth-server-metadata_configuring-internal-oauth
    export LAUNCHER_OAUTH_OPENSHIFT_URL=https://oauth-openshift.apps-crc.testing/oauth/authorize
    export LAUNCHER_OAUTH_OPENSHIFT_CLIENT_ID=launcher
    ;;
*)
    echo ERROR: Failed to setup environment. Please choose a LAUTH mode.
    [ $PS1 ] && return || exit;
    ;;
esac

# This will be set to "staging" on a staging server and "production" on a production server
  #export LAUNCHER_BACKEND_ENVIRONMENT=development
  #export LAUNCHER_BACKEND_SENTRY_DSN=???

# This will prevent boosters being downloaded at startup making development faster (default = true)
  export LAUNCHER_PREFETCH_BOOSTERS=false

# For launchpad-booster-catalog-service
  #export LAUNCHER_BOOSTER_CATALOG_REPOSITORY=https://github.com/fabric8-launcher/launcher-booster-catalog.git
  #export LAUNCHER_BOOSTER_CATALOG_REF=master

# This can be used to filter boosters depending on their properties
# Eg. `booster.mission.id == 'rest-http'`
  #export LAUNCHER_BOOSTER_CATALOG_FILTER=

# The supported Git providers
  #eport LAUNCHER_GIT_PROVIDER=GitHub
  export LAUNCHER_GIT_PROVIDERS_FILE=$SCRIPT_DIR/git-providers.yaml

# For launchpad-frontend
  #export LAUNCHER_MISSIONCONTROL_URL="ws://localhost:8080"
  #export LAUNCHER_BACKEND_URL="http://localhost:8080/api"
  #export LAUNCHER_CREATOR_ENABLED=true
  #export LAUNCHER_CREATOR_URL="http://localhost:8080/api/creator"
  #export LAUNCHER_CREATOR_SENTRY_DSN=???
  #export LAUNCHER_TARGETENVIRONMENT_SKIP=false
  #export LAUNCHER_FRONTEND_SENTRY_DSN=???

# Testing tracker token
  export LAUNCHER_TRACKER_SEGMENT_TOKEN=dMV5AjaweCpO3KZop7TuZ0961UO74AF0

case "$ECHO_ENV" in
"YES")
    # Display LAUNCHER environment
    env | grep 'LAUNCHER_'
    ;;
esac


