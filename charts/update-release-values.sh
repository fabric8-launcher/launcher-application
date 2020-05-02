#!/bin/sh

echo -e "application:\n  image:\n    tag: $(curl -s https://api.github.com/repos/fabric8-launcher/launcher-application/commits/HEAD | jq -r '.sha' | cut -c1-7)" > release-values.yaml
echo -e "welcome:\n  image:\n    tag: $(curl -s https://api.github.com/repos/fabric8-launcher/launcher-creator-welcome-app/commits/HEAD | jq -r '.sha' | cut -c1-7)" >> release-values.yaml
