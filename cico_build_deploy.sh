#!/usr/bin/bash

GENERATOR_DOCKER_HUB_USERNAME=openshiftioadmin
REGISTRY_URI="push.registry.devshift.net"
REGISTRY_NS="fabric8"
REGISTRY_IMAGE="launcher-backend"
DOCKER_HUB_URL=${REGISTRY_NS}/${REGISTRY_IMAGE}
BUILDER_IMAGE="launcher-backend-builder"
BUILDER_CONT="launcher-backend-builder-container"
DEPLOY_IMAGE="launcher-backend-deploy"

if [ "$TARGET" = "rhel" ]; then
    REGISTRY_URL=${REGISTRY_URI}/osio-prod/${DOCKER_HUB_URL}
    DOCKERFILE_DEPLOY="Dockerfile.deploy.rhel"
else
    REGISTRY_URL=${REGISTRY_URI}/${DOCKER_HUB_URL}
    DOCKERFILE_DEPLOY="Dockerfile.deploy"
fi

TARGET_DIR="target"

docker_login() {
    local USERNAME=$1
    local PASSWORD=$2
    local REGISTRY=$3

    if [ -n "${USERNAME}" ] && [ -n "${PASSWORD}" ]; then
        docker login -u ${USERNAME} -p ${PASSWORD} ${REGISTRY}
    fi
}

tag_push() {
    local TARGET_IMAGE=$1

    docker tag ${DEPLOY_IMAGE} ${TARGET_IMAGE}
    docker push ${TARGET_IMAGE}
}

# Exit on error
set -e

if [ -z "$CICO_LOCAL" ]; then
    [ -f jenkins-env ] && cat jenkins-env | grep -e PASS -e GIT -e DEVSHIFT -e SONAR > inherit-env
    [ -f inherit-env ] && . inherit-env

    # We need to disable selinux for now, XXX
    /usr/sbin/setenforce 0 || :

    # Get all the deps in
    yum -y install docker make git
    service docker start
fi

#BUILD
if [ ! -d "${TARGET_DIR}" ]; then
    mkdir ${TARGET_DIR}

    docker build -t ${BUILDER_IMAGE} -f Dockerfile.build .

    docker run --detach=true --name ${BUILDER_CONT} -t -v $(pwd)/${TARGET_DIR}:/${TARGET_DIR}:Z ${BUILDER_IMAGE} /bin/tail -f /dev/null #FIXME

    docker exec ${BUILDER_CONT} mvn -B clean install -DskipTests -Ddownload.plugin.skip.cache
    docker exec -u root ${BUILDER_CONT} cp web/target/launcher-backend-swarm.jar /${TARGET_DIR}
fi

#PUSH
if [ -z "$CICO_LOCAL" ]; then
    docker_login ${DEVSHIFT_USERNAME} ${DEVSHIFT_PASSWORD} ${REGISTRY_URI}

    docker build -t ${DEPLOY_IMAGE} -f "${DOCKERFILE_DEPLOY}" .

    TAG=$(echo $GIT_COMMIT | cut -c1-${DEVSHIFT_TAG_LEN})

    tag_push "${REGISTRY_URL}:${TAG}"
    tag_push "${REGISTRY_URL}:latest"

    if [[ "$TARGET" != "rhel" && -n "${GENERATOR_DOCKER_HUB_PASSWORD}" ]]; then
        docker_login ${GENERATOR_DOCKER_HUB_USERNAME} ${GENERATOR_DOCKER_HUB_PASSWORD}

        tag_push "${DOCKER_HUB_URL}:${TAG}"
        tag_push "${DOCKER_HUB_URL}:latest"
    fi
fi

#SONAR
if [ -n "${SONAR_LOGIN}" ]; then
    docker exec ${BUILDER_CONT} mvn sonar:sonar -Dsonar.organization=fabric8-launcher -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=${SONAR_LOGIN}
fi
