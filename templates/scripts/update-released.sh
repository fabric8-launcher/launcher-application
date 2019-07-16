#!/bin/sh

echo BACKEND_IMAGE_TAG=$(curl -s https://api.github.com/repos/fabric8-launcher/launcher-application/commits/HEAD | jq -r '.sha' | cut -c1-7) >> released.properties
echo WELCOME_IMAGE_TAG=$(curl -s https://api.github.com/repos/fabric8-launcher/launcher-creator-welcome-app/commits/HEAD | jq -r '.sha' | cut -c1-7) >> released.properties
