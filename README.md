# Fabric8-Launcher Backend

[![Build Status](https://ci.centos.org/view/Devtools/job/devtools-launcher-backend-generator-build-master/badge/icon)](https://ci.centos.org/view/Devtools/job/devtools-launcher-backend-generator-build-master/)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=io.fabric8.launcher%3Alauncher-parent&metric=alert_status)](https://sonarcloud.io/dashboard/index/io.fabric8.launcher:launcher-parent)

The launcher-backend is a Java EE 7 application exposing several JAX-RS endpoints to handle launching of application into Openshift. The backend runs within a WildFly Swarm container and is called from
an Angular Front application responsible to collect from an end user the information needed to generate
a Zip file containing a Maven project populated for an Eclipse Vert.x, Spring Boot, WildFly Swarm or Node.js
container (see https://github.com/fabric8-launcher/launcher-booster-catalog for the full list).

The OpenAPI 3.0 descriptor for these services is available at https://editor.swagger.io/?url=https://forge.api.openshift.io/swagger.yaml

Contributions
-------------

Contributions are welcome!

Please read the [Contributing Guide](./CONTRIBUTING.md) to help you make a great contribution.

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

 
IDE Setup
---------

 * *Immutables* - Setup your IDE for dealing with generated immutable classes see the
   [online documentation](https://immutables.github.io/apt.html). You have to build the project at least
   once for the classes to be generated or you will still get errors in the IDE.

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

        $ curl -v -H "Content-Type: application/json" -d '{}' -X POST  https://localhost:8180/api/booster-catalog/reindex\?token\=TOKEN
        
        
Setup git providers default credentials (No KeyCloak mode)
----------------------------------------------------------


#### GitHub

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

#### GitLab
 
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

#### BitBucket


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


Filtering the booster catalog
-----------------------------

This feature can be activated by providing the `LAUNCHER_BOOSTER_CATALOG_FILTER` env param/system property
The script must evaluate to a boolean using the `booster` variable, that is an instance of [RhoarBooster](https://github.com/fabric8-launcher/launcher-booster-catalog-service/blob/master/src/main/java/io/fabric8/launcher/booster/catalog/rhoar/RhoarBooster.java).

        $ export LAUNCHER_BOOSTER_CATALOG_FILTER=booster.mission.id === 'rest-http'

Examples:

- `booster.mission.id == 'rest-http'`: will return only HTTP Rest mission boosters
- `booster.runtime.id == 'spring-boot'`: returns only the Spring Boot boosters
- `booster.mission.id == 'rest-http' && booster.runtime.id` == 'spring-boot': returns only HTTP Rest Spring Boot boosters
- `booster.metadata.istio`: returns only boosters that contains the `istio: true` flag in the booster metadata
- `booster.mission.metadata.istio`: returns only boosters that contains the `istio: true` flag in the mission metadata assigned to the booster





Code of Conduct
-------------

Please, adopt our [Code of Conduct](./CODE_OF_CONDUCT.md) to follow our community standards, signal a welcoming and inclusive project, and outline procedures for handling abuse.

