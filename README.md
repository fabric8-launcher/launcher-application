# Launcher Backend

[![Build Status](https://ci.centos.org/view/Devtools/job/devtools-launcher-backend-generator-build-master/badge/icon)](https://ci.centos.org/view/Devtools/job/devtools-launcher-backend-generator-build-master/)

This code generator project which is a Java backend system exposes several JBoss Forge commands
using a REST endpoint. The backend runs within a WildFly Swarm container and is called from
an Angular Front application responsible to collect from an end user the information needed to generate
a Zip file containing a Maven project populated for an Eclipse Vert.x, Spring Boot, WildFly Swarm or Node.js
container.

 
Setting up the environment
--------------------------


* You have to setup environment variables before you start the back-end. 

> You can source, or use as an example, the environment script template located there: [./launcher-env-template.sh](./launcher-env-template.sh)

```bash
$ source ./launcher-env-template.sh
```

> [KeyCloak](http://www.keycloak.org/) adds authentication to the Launcher and secures services with minimum fuss. No need to deal with storing users or authenticating users. It's all available out of the box.
> For development only, you can choose to work without KeyCloak by changing the option in the environment script.
> If you are not using KeyCloak, you can find information on how to [setup your git providers default credentials](README.md#setup-git-providers-default-credentials-no-keycloak-mode).

 
Build and Run the Application
-----------------------------

* First follow the environment setup [instructions](README.md#setting-up-the-environment). 

* Build this project:
```bash
$ mvn clean install
```

* Run:
```bash
$ java -jar web/target/launcher-backend-swarm.jar
```

Then follow the [front-end README](https://github.com/fabric8-launcher/launcher-frontend/blob/master/README.md) to run the front-end.

Running with Docker
-------------------

* First follow the environment setup [instructions](README.md#setting-up-the-environment). 

* Build and run:
```bash
$ ./docker.sh
```

This will build and run a Docker image that will work in the same way as if you were running it locally. So the frontend can connect to it in the same way.

Build and Run the Unit Tests
----------------------------

* First follow the environment setup [instructions](README.md#setting-up-the-environment). 

* Execute:
```bash
$ mvn clean install
```
        
Build and Run the Unit and Integration Tests
--------------------------------------------

* First follow the environment setup [instructions](README.md#setting-up-the-environment). 

* To build the project and run the integration tests, allowing Maven to start the WildFly Swarm server:
```bash
$ mvn clean install -Pit
```

* To skip building and just run the integration tests, allowing Maven to start the WildFly Swarm server:
```bash
$ mvn integration-test -Pit
```

Reindex the booster catalog
---------------------------

Run the following command, replace TOKEN with the value defined in the environment variable `LAUNCHER_BACKEND_CATALOG_REINDEX_TOKEN`. Doesn't need to be specified if the environment variable doesn't exist in the running environment:

        $ curl -v -H "Content-Type: application/json" -d '{}' -X POST  https://localhost:8180/api/launchpad/catalog/reindex\?token\=TOKEN
        
        
Setup git providers default credentials (No KeyCloak mode)
----------------------------------------------------------

* Log into GitHub and generate an access token for use here:
--  https://github.com/settings/tokens
    * Set scopes
        * `repo`
        * `admin:repo_hook`
        * `delete_repo`
* Run the following commands:
 ```bash
 git config --global github.user "<replace with your github username>"
 git config --global github.token "<replace with your github token>"
 ```
 
* Log into GitLab and generate an access token for use here: 
--  https://gitlab.com/profile/personal_access_tokens
    * Set scopes
        * `api`
        * `read_user`
* Run the following commands:
 ```bash
 git config --global gitlab.user "<replace with your gitlab username>"
 git config --global gitlab.token "<replace with your gitlab token>"
 ```
              
* Log into Bitbucket and generate an application password for use here: 
--  https://bitbucket.org/account/admin/app-passwords
    * Activate permissions:
        * Account: Email, Read
        * Team: Read
        * Projects: Read
        * Repositories: Read, Write, Admin, Delete
        * Pull requests: Read
        * Issue: Read
        * Webhook: Read and write
* Run the following commands:
 ```bash
 git config --global bitbucket.user "<replace with your github username>"
 git config --global bitbucket.password "<replace with your bitbucket application password>"
 ```


