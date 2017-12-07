#!/bin/bash

DO_BUILD=1
DO_RUN=1

# see if a "--net" option was passed, if so we'll connect the
# container to a private network (creating it if necessary)
NETWORK=default
DRUN_OPTS=""
for arg; do
    case $arg in
        --net)	NETWORK=launchernw
				# create a docker network for our app if it doesn't exist
				if ! docker network ls | grep -q $NETWORK; then docker network create $NETWORK; fi
				;;
        --build) DO_RUN=0
                ;;
        --run) DO_BUILD=0
                ;;
        --help) echo "Usage: docker.sh [options]"
                echo ""
                echo "Builds and runs this project's Docker image"
                echo ""
                echo "Options:"
                echo "   --build  : Only build the Docker image"
                echo "   --run    : Only run the Docker image"
                echo "   --net    : When run the Docker image will be attached to a private network"
                echo "   --help   : This help"
                echo ""
                echo "For all other available options see 'docker run --help'"
                exit
                ;;
        *)	DRUN_OPTS="$DRUN_OPTS ${arg}"
				;;
    esac
done

if [[ $DO_BUILD -eq 1 ]]; then
    # remove any pre-existing image
    docker rm -f launcher-backend >/dev/null 2>&1

    # build the image
    echo "Building image..."
    mkdir -p target
    cp web/target/launcher-backend-swarm.jar target/
    docker build -q -t fabric8/launcher-backend -f Dockerfile.deploy .
fi

if [[ $DO_RUN -eq 1 ]]; then
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
        -eLAUNCHER_BACKEND_CATALOG_GIT_REPOSITORY=$LAUNCHER_BACKEND_CATALOG_GIT_REPOSITORY \
        -eLAUNCHER_BACKEND_CATALOG_GIT_REF=$LAUNCHER_BACKEND_CATALOG_GIT_REF \
        -eLAUNCHER_TRACKER_SEGMENT_TOKEN=$LAUNCHER_TRACKER_SEGMENT_TOKEN \
        $DRUN_OPTS \
        fabric8/launcher-backend
fi

