#!/usr/bin/bash


REGISTRY_URI="registry.devshift.net"
REGISTRY_NS="obsidian"
REGISTRY_IMAGE="obsidian-generator-backend:latest"
REGISTRY_URL=${REGISTRY_URI}/${REGISTRY_NS}/${REGISTRY_IMAGE}
DOCKER_HUB_URL="redhatdevelopers/generator-backend"
BUILDER_IMAGE="obsidian-generator-backend-builder"
BUILDER_CONT="obsidian-generator-backend-builder-container"
DEPLOY_IMAGE="obsidian-generator-backend-deploy"

TARGET_DIR="target"

# Show command before executing
set -x

# Exit on error
set -e

if [ -z $CICO_LOCAL ]; then
    cat jenkins-env | grep -e PASS -e USER > inherit-env
    . inherit-env

    # We need to disable selinux for now, XXX
    /usr/sbin/setenforce 0

    # Get all the deps in
    yum -y install docker make git

    # Get all the deps in
    yum -y install docker make git
    service docker start
fi

#CLEAN
docker ps | grep -q ${BUILDER_CONT} && docker stop ${BUILDER_CONT}
docker ps -a | grep -q ${BUILDER_CONT} && docker rm ${BUILDER_CONT}
rm -rf ${TARGET_DIR}/

#BUILD
docker build -t ${BUILDER_IMAGE} -f Dockerfile.build .

mkdir ${TARGET_DIR}/
docker run --detach=true --name ${BUILDER_CONT} -t -v $(pwd)/${TARGET_DIR}:/${TARGET_DIR}:Z ${BUILDER_IMAGE} /bin/tail -f /dev/null #FIXME

docker exec ${BUILDER_CONT} mvn -B clean install
docker exec -u root ${BUILDER_CONT} cp ${TARGET_DIR}/generator-swarm.jar /${TARGET_DIR}

#BUILD DEPLOY IMAGE
docker build -t ${DEPLOY_IMAGE} -f Dockerfile.deploy .

#PUSH
if [ -z $CICO_LOCAL ]; then
    docker tag ${DEPLOY_IMAGE} ${REGISTRY_URL}
    docker push ${REGISTRY_URL}

    docker tag ${DEPLOY_IMAGE} ${DOCKER_HUB_URL}
    docker login -u ${GENERATOR_DOCKER_HUB_USERNAME} -p ${GENERATOR_DOCKER_HUB_PASSWORD} -e noreply@redhat.com
    docker push ${DOCKER_HUB_URL}
fi
