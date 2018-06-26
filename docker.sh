#!/bin/bash

DO_BUILD=1
DO_RUN=1
USE_KEYCLOAK=0

SCRIPT_DIR=$(cd "$(dirname "$BASH_SOURCE")" ; pwd -P)

# see if a "--net" option was passed, if so we'll connect the
# container to a private network (creating it if necessary)
NETWORK=default
DRUN_OPTS=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --net)	NETWORK=launchernw
                # create a docker network for our app if it doesn't exist
                if ! docker network ls | grep -q $NETWORK; then docker network create $NETWORK; fi
                ;;
        --build) DO_RUN=0
                ;;
        --run) DO_BUILD=0
                ;;
        --ghuser) LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME="$2"
                shift
                ;;
        --ghtoken) LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN="$2"
                shift
                ;;
        --keycloak) USE_KEYCLOAK=1
                ;;
        --help) echo "Usage: docker.sh [options]"
                echo ""
                echo "Builds and runs this project's Docker image"
                echo ""
                echo "Options:"
                echo "   --build           : Only build the Docker image"
                echo "   --run             : Only run the Docker image"
                echo "   --ghuser <user>   : Sets/overrides the GitHub user to use when running"
                echo "   --ghtoken <token> : Sets/overrides the GitHub token to use when running"
                echo "   --net             : When run the Docker image will be attached to a private network"
                echo "   --keycloak        : Don't use local minishift but official keycloak server"
                echo "   --help            : This help"
                echo ""
                echo "For all other available options see 'docker run --help'"
                exit
                ;;
        *)	DRUN_OPTS="$DRUN_OPTS $1"
				;;
    esac
    shift
done

if [[ $DO_BUILD -eq 1 ]]; then
    # Build the image
    echo "Building image..."
    mkdir -p target
    cp web/target/launcher-backend-thorntail.jar target/
    docker build -t fabric8/launcher-backend -f Dockerfile.deploy .
fi

if [[ $DO_RUN -eq 1 ]]; then
    # Remove any pre-existing container
    docker rm -f launcher-backend >/dev/null 2>&1
    
    if [[ $USE_KEYCLOAK -eq 1 ]]; then
        # Authentication: Official keycloak
        export LAUNCHER_KEYCLOAK_URL=https://sso.openshift.io/auth
        export LAUNCHER_KEYCLOAK_REALM=rh-developers-launch
        export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE=/clusters.yaml
        unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL
        unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL
        unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME
        unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD
        unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN
        unset LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME
        unset LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN
    else
        # Check if environment contains required variables
        [[ -z "${LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME}" ]] && LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME=$(git config github.user)
        [[ -z "${LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN}" ]] && LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN=$(git config github.token)
        if [[ -z "${LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME}" || -z "${LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN}" ]]; then
            echo "You need to at least set the following environment variables:"
            echo "   export LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME=<your GitHub user name>"
            echo "   export LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN=<your GitHub token>"
            echo "Or set them using the --ghuser and --ghtoken options for this script, exiting."
            exit
        fi
        if [[ -z "${LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL}" ]]; then
            # Check if minishift exists and is running
            if hash minishift 2>/dev/null && ! minishift status >/dev/null 2>&1 | grep -qi running; then
                echo "Minishift found, running with default values..."
                MSHIFT=$(minishift console --url)
                export LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL=$MSHIFT
                export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=$MSHIFT
                # Authentication: No KeyCloak
                unset LAUNCHER_KEYCLOAK_URL
                unset LAUNCHER_KEYCLOAK_REALM
                unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE
                unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN
                export LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME=developer
                export LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD=developer
            else
                echo "Minishift doesn't seem to be running, exiting."
                exit
            fi
        fi
    fi
    # For launchpad-backend
    export LAUNCHER_BOOSTER_CATALOG_REPOSITORY=https://github.com/fabric8-launcher/launcher-booster-catalog.git
    export LAUNCHER_BOOSTER_CATALOG_REF=master
    export LAUNCHER_PREFETCH_BOOSTERS=false
    # For OSIO addon in the backend
    export WIT_URL=https://api.openshift.io
    export AUTH_URL=https://auth.openshift.io
    export KEYCLOAK_SAAS_URL=https://sso.openshift.io/
    export OPENSHIFT_API_URL=https://f8osoproxy-test-dsaas-production.09b5.dsaas.openshiftapps.com
    export JENKINS_URL=https://jenkins.openshift.io
    export F8A_ANALYTICS_RECOMMENDER_API_URL=https://recommender.api.openshift.io
    # For OSIO frontend
    export FABRIC8_FORGE_API_URL=http://localhost:8080
	
    # run it
    echo "Running image..."
    docker run \
        --name launcher-backend \
        --network $NETWORK \
        -t \
        -p8080:8080 \
        -eLAUNCHER_KEYCLOAK_URL=$LAUNCHER_KEYCLOAK_URL \
        -eLAUNCHER_KEYCLOAK_REALM=$LAUNCHER_KEYCLOAK_REALM \
        -eLAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE=LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE \
        -eLAUNCHER_MISSIONCONTROL_GITHUB_USERNAME=$LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME \
        -eLAUNCHER_MISSIONCONTROL_GITHUB_TOKEN=$LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN \
        -eLAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL=$LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL \
        -eLAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=$LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL \
        -eLAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME=$LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME \
        -eLAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD=$LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD \
        -eLAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN=$LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN \
        -eLAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE=$LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE \
        -eLAUNCHER_MISSIONCONTROL_SERVICE_HOST=$LAUNCHER_MISSIONCONTROL_SERVICE_HOST \
        -eLAUNCHER_MISSIONCONTROL_SERVICE_PORT=$LAUNCHER_MISSIONCONTROL_SERVICE_PORT \
        -eLAUNCHER_BOOSTER_CATALOG_REPOSITORY=$LAUNCHER_BOOSTER_CATALOG_REPOSITORY \
        -eLAUNCHER_CATALOG_GIT_REF=$LAUNCHER_CATALOG_GIT_REF \
        -eLAUNCHER_TRACKER_SEGMENT_TOKEN=$LAUNCHER_TRACKER_SEGMENT_TOKEN \
        -v $SCRIPT_DIR/clusters.yaml:/clusters.yaml \
        $DRUN_OPTS \
        fabric8/launcher-backend
fi

