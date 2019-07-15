#!/bin/bash

SCRIPT_DIR=$(cd "$(dirname "$BASH_SOURCE")" ; pwd -P)

if [[ -t 0 ]]; then
    BASE=$SCRIPT_DIR/../..
    PROPS=$(cat $BASE/released.properties)
else
    BASE="https://raw.githubusercontent.com/fabric8-launcher/launcher-application/master/templates"
    PROPS=$(curl -s $BASE/released.properties)
fi

case "$1" in
    ""|"--released")
        ;;
    "--latest")
        PROPS=
        ;;
    "--delete")
        oc delete project launcher
        exit
        ;;
    *)
        echo "Invalid argument: $1"
        echo "Usage: install [--released] [--latest] [--delete]"
        exit
        ;;
esac

for p in $PROPS; do
    PARAMS="$PARAMS -p $p"
done

echo This script will install the Launcher in Minishift. Make sure that:
echo 
echo  - Minishift is running 
echo  - You have run oc login previously
echo  - Your GitHub Username is correct [found from git config github.user]: $(git config github.user)
echo  - Your GitHub Token is correct [found from git config github.token]: *REDACTED*
echo 
echo Press ENTER to continue ...
read 

echo Creating launcher project ...
oc new-project launcher

echo Processing the template and installing $BASE/openshift/launcher-template.yaml ...
oc process --local -f $BASE/openshift/launcher-template.yaml \
   LAUNCHER_KEYCLOAK_URL= \
   LAUNCHER_KEYCLOAK_REALM= \
   LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN=$(git config github.token) \
   LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=$(minishift console --url | sed 's/\/console//') \
   $PARAMS -o yaml | oc create -f -

echo All set! Enjoy!

