# Launcher Backend

[![Build Status](https://ci.centos.org/view/Devtools/job/devtools-launcher-backend-generator-build-master/badge/icon)](https://ci.centos.org/view/Devtools/job/devtools-launcher-backend-generator-build-master/)

This code generator project which is a Java backend system exposes several JBoss Forge commands
using a REST endpoint. The backend runs within a WildFly Swarm container and is called from
an Angular Front application responsible to collect from an end user the information needed to generate
a Zip file containing a Maven project populated for an Eclipse Vert.x, Spring Boot, WildFly Swarm or Node.js
container.

The First Time
--------------

 * Log into GitHub and generate an access token for use here:
  --  https://github.com/settings/tokens
   * Set scopes
     * `repo`
     * `admin:repo_hook`
     * `delete_repo`
 * Run the following commands once:
 ```bash
 git config --global github.user "<replace with your github username>"
 git config --global github.token "<replace with your github token>"
 ```
 *  Log into GitLab and generate an access token for use here:
     --  https://gitlab.com/profile/personal_access_tokens
      * Set scopes
        * `api`
        * `read_user`
 * Run the following commands once:
 ```bash
 git config --global gitlab.user "<replace with your gitlab username>"
 git config --global gitlab.token "<replace with your gitlab token>"
 ```
 
Build and Run the Application
-----------------------------

* Build this project:
```bash
$ mvn clean install
```

* Execute the application in the easiest, most generic manner with:
```bash
$ ./run.sh
```

* Or for more control regarding how the application should behave and what services it should connect to etc, edit and source the [the script defined below](README.md#setting-up-the-environment) and run:
```bash
$ java -jar web/target/launcher-backend-swarm.jar
```

Then follow the [front-end README](https://github.com/fabric8-launcher/launcher-frontend/blob/master/README.md) to run the front-end.

Running with Docker
-------------------

* First source the [the script defined below](README.md#setting-up-the-environment)
* Build and run:
```bash
$ ./docker.sh
```

This will build and run a Docker image that will work in the same way as if you were running it locally. So the frontend can connect to it in the same way.

Build and Run the Unit Tests
----------------------------

* Execute:

        $ mvn clean install

Prerequisites to Run Integration Tests
--------------------------------------

1. A GitHub Account

    * Create 2 environment variables:
        * `LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME`
        * `LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN`

    For instance you may create a `~/launcher-missioncontrol-env.sh` file and add:
    
        #!/bin/sh
        export LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME=<your github username>
        export LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN=<token created from above>
    
    You can also reuse what's already defined in your `.gitconfig` file:
    
        #!/bin/sh
        export LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME=`git config github.user`
        export LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN=`git config github.token`

    Use `source ~/launcher-missioncontrol-env.sh` to make your changes visible; you may check by typing into a terminal:

        $ echo $LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME
        $ echo $LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN
    
2. A GitLab Account

    * Create 2 environment variables:
        * `LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME`
        * `LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN`

    For instance you may create a `~/launcher-missioncontrol-env.sh` file and add:

        #!/bin/sh
        export LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME=<your gitlab username>
        export LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN=<token created from above>

    You can also reuse what's already defined in your `.gitconfig` file:

        #!/bin/sh
        export LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME=`git config gitlab.user`
        export LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN=`git config gitlab.token`

    Use `source ~/launcher-missioncontrol-env.sh` to make your changes visible; you may check by typing into a terminal:

        $ echo $LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME
        $ echo $LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN

3. A locally-running instance of OpenShift

    * Install minishift and prerequisite projects by following these instructions
        * https://github.com/minishift/minishift#installing-minishift
	
    * Check everything works okay by loggin in to the OpenShift console
        * Run `minishift start --memory=4096`
        * Open the URL found in the output of the previous command in a browser. You can get the same URL by executing `minishift console --url` as well.
        * Log in with user `developer` and password `developer`
        * You may have to accept some security exceptions in your browser because of missing SSL Certificates

    * Set up the following environment variables (possibly in your `launcher-missioncontrol-env.sh` file):
        ```
        LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL=<insert minishift console url something like https://192.168.99.128:8443>
        LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=<insert minishift console url something like https://192.168.99.128:8443>
        ```
        
        You can do this automatically in the following way:       
        ```
        export LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL=`minishift console --url`
        export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=`minishift console --url`
        ```

4. A Keycloak server

    * Make sure your Federated Identity settings are correct
        * Open Chrome and go to: https://prod-preview.openshift.io/
        * Click Sign-in (in the upper right corner), you should be redirected to developers.redhat.com
        * Navigate to https://sso.prod-preview.openshift.io/auth/realms/fabric8/account/identity
        * Make sure that the Github and Openshift v3 tokens are set

    * Set up the following environment variables (possibly in your `launcher-missioncontrol-env.sh` file): 
      ```
        export LAUNCHER_KEYCLOAK_URL=https://sso.prod-preview.openshift.io/auth
        export LAUNCHER_KEYCLOAK_REALM=fabric8
      ```
    IMPORTANT: Mission Control will not use the keycloak server if you provide the following environment variables:
      ```    
        export LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME=<user>
        export LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD=<pass>
      ```

5. Testing setup

   * Make sure you refer to a trust store used for Service Virtualization tests:
     ```
     export LAUNCHER_TESTS_TRUSTSTORE_PATH=${PWD}/services/git-service-impl/src/test/resources/hoverfly/hoverfly.jks
     ```    

6. (Optional) Ensure from the previous steps all environment variables are properly set up and sourced into your terminal. You can use [the script defined below](README.md#setting-up-the-environment) to do that for you.

Run the OSIO addon
------------------
See how to run the [fabric8-ui project](https://github.com/fabric8-ui/fabric8-ui) and set the environment variables to either prod, pre-prod or dev.
Then override the `FABRIC8_FORGE_API_URL` with the instance that you are running like this:
```
export FABRIC8_FORGE_API_URL=http://localhost:8080
```
Before starting this project set `WIT_URL`, `AUTH_URL`, `KEYCLOAK_SAAS_URL` and `OPENSHIFT_API_URL` for prod this would be:
```
export WIT_URL=https://api.openshift.io
export AUTH_URL=https://auth.openshift.io
export KEYCLOAK_SAAS_URL=https://sso.openshift.io/
export OPENSHIFT_API_URL=https://api.starter-us-east-2.openshift.com
```
        
Run the Integration Tests, Optionally Building
----------------------------------------------

* You need to set the following environment variables before running the integration tests: 

		$ export LAUNCHER_MISSIONCONTROL_SERVICE_HOST=localhost
		$ export LAUNCHER_MISSIONCONTROL_SERVICE_PORT=8080


* To build the project and run the integration tests, allowing Maven to start the WildFly Swarm server:
 
        $ mvn clean install -Pit


* To skip building and just run the integration tests, allowing Maven to start the WildFly Swarm server:

        $ mvn integration-test -Pit

Reindex the booster catalog
---------------------------

Run the following command, replace TOKEN with the value defined in the environment variable `LAUNCHER_BACKEND_CATALOG_REINDEX_TOKEN`. Doesn't need to be specified if the environment variable doesn't exist in the running environment:

        $ curl -v -H "Content-Type: application/json" -d '{}' -X POST  https://localhost:8180/api/launchpad/catalog/reindex\?token\=TOKEN

Setting up the environment
--------------------------

In a Unix-like environment you may like to create a `launcher-missioncontrol-env.sh` file to hold the following; this may be executed using `source launchpad-missioncontrol-env.sh`. You can copy & paste the following script (but be sure to have followed the [The First Time](README.md#the-first-time) setup instructions above):

```
#!/bin/sh

SCRIPT_DIR=$(dirname "$BASH_SOURCE")

# Setting up authentication for the various services
MSHIFT=$(minishift console --url)
if [[ $MSHIFT == *"://:"* ]]; then
    echo "WARNING: MiniShift is NOT running, the environment variables will NOT be properly set!"
fi
export LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL=$MSHIFT
export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=$MSHIFT

export LAUNCHER_MISSIONCONTROL_GITHUB_USERNAME=`git config github.user`
export LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN=`git config github.token`
export LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME=`git config gitlab.user`
export LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN=`git config gitlab.token`

# Choose one of the 3 KeyCloak options below
# (uncomment the lines of your choice, making sure all other options are commented out fully)

# A) No KeyCloak
unset LAUNCHER_KEYCLOAK_URL
unset LAUNCHER_KEYCLOAK_REALM
unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE
export LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME=developer
export LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD=developer
# If set, will override username/password authentication scheme
#export LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN=<token here>
unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN

# B) Official KeyCloak
#export LAUNCHER_KEYCLOAK_URL=https://sso.openshift.io/auth
#export LAUNCHER_KEYCLOAK_REALM=rh-developers-launch
#export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE=$SCRIPT_DIR/clusters.yaml
#unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME
#unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD
#unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN

# C) Local KeyCloak
#export LAUNCHER_KEYCLOAK_URL=http://localhost:8280/auth
#export LAUNCHER_KEYCLOAK_REALM=launch
#export LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE=$SCRIPT_DIR/clusters.yaml
#unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME
#unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD
#unset LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN

# For launchpad-backend
export LAUNCHER_MISSIONCONTROL_SERVICE_HOST=localhost
export LAUNCHER_MISSIONCONTROL_SERVICE_PORT=8080

# This will be set to "staging" on a staging server and "production" on a production server
export LAUNCHER_BACKEND_ENVIRONMENT=development
# This will prevent boosters being downloaded at startup making development faster
export LAUNCHER_PREFETCH_BOOSTERS=false

# For launchpad-booster-catalog-service
export LAUNCHER_BOOSTER_CATALOG_REPOSITORY=https://github.com/fabric8-launcher/launcher-booster-catalog.git
export LAUNCHER_BOOSTER_CATALOG_REF=master

# For launchpad-frontend
export LAUNCHER_MISSIONCONTROL_URL="ws://127.0.0.1:8080"
export LAUNCHER_BACKEND_URL="http://127.0.0.1:8080/api"

# Testing tracker token
export LAUNCHER_TRACKER_SEGMENT_TOKEN=dMV5AjaweCpO3KZop7TuZ0961UO74AF0

# For OSIO addon in the backend
export WIT_URL=https://api.openshift.io
export AUTH_URL=https://auth.openshift.io
export KEYCLOAK_SAAS_URL=https://sso.openshift.io/
export OPENSHIFT_API_URL=https://api.starter-us-east-2.openshift.com

# For OSIO frontend
export FABRIC8_FORGE_API_URL=http://localhost:8080

# For Integration Tests
export LAUNCHER_TESTS_TRUSTSTORE_PATH=${PWD}/services/git-service-impl/src/test/resources/hoverfly/hoverfly.jks
``` 

