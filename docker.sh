#!/bin/bash

DO_BUILD=1
DO_RUN=1

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
    cp web/target/launcher-backend-swarm.jar target/
    docker build -q -t fabric8/launcher-backend -f Dockerfile.deploy .
fi

if [[ $DO_RUN -eq 1 ]]; then
    # Remove any pre-existing container
    docker rm -f launcher-backend >/dev/null 2>&1
    
    # Check if environment contains required variables
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
            # For launchpad-backend
            export LAUNCHER_MISSIONCONTROL_SERVICE_HOST=localhost
            export LAUNCHER_MISSIONCONTROL_SERVICE_PORT=8080
            export LAUNCHER_BOOSTER_CATALOG_REPOSITORY=https://github.com/fabric8-launcher/launcher-booster-catalog.git
            export LAUNCHER_BOOSTER_CATALOG_REF=master
            # For OSIO addon in the backend
            export WIT_URL=https://api.openshift.io
            export AUTH_URL=https://auth.openshift.io
            export KEYCLOAK_SAAS_URL=https://sso.openshift.io/
            export OPENSHIFT_API_URL=https://api.starter-us-east-2.openshift.com
        else
            echo "Required environment variables not found, exiting."
            exit
        fi
    fi
	
    # run it
    echo "Running image..."
    docker run \
        --name launcher-backend \
        --network $NETWORK \
        -t \
        -p8080:8080 \
        -eLAUNCHER_KEYCLOAK_URL=$LAUNCHER_KEYCLOAK_URL \
        -eLAUNCHER_KEYCLOAK_REALM=$LAUNCHER_KEYCLOAK_REALM \
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
        $DRUN_OPTS \
        fabric8/launcher-backend
fi

