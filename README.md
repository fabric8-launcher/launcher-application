# Launchpad Backend

[![Build Status](https://ci.centos.org/view/Devtools/job/devtools-launchpad-backend-generator-build-master/badge/icon)](https://ci.centos.org/view/Devtools/job/devtools-launchpad-backend-generator-build-master/)

This code generator project which is a Java backend system exposes several JBoss Forge commands
using a REST endpoint. The backend runs within a WildFly Swarm container and is called from
an Angular Front application responsible to collect from an end user the information needed to generate
a Zip file containing a Maven project populated for an Eclipse Vert.x, Spring Boot or WildFly Swarm
container.

* Build this project:

```bash
$ mvn clean package -s configuration/settings.xml
```

Remark : This project requires that you compile this [github project](http://github.com/openshiftio/launchpad-addon).

* Set the following environment variables: 

		$ export LAUNCHPAD_MISSIONCONTROL_SERVICE_HOST=localhost
		$ export LAUNCHPAD_MISSIONCONTROL_SERVICE_PORT=8080

* Execute the uber-jar in the target folder with:

```bash
$ java -Dswarm.port.offset=100 -jar target/launchpad-backend-swarm.jar
```

Then follow the [front-end ReadMe][1] to run the front-end.

[1]:https://github.com/openshiftio/launchpad-frontend/blob/master/README.md

## Development mode

Run with the `-DdevMode=true` flag to auto-reload SNAPSHOT addons that are installed in your local maven repository. The changes will last as long as the container is alive.
Make sure to rebuild the launchpad-backend if for some reason you need to stop the container:
```
mvn -DdevMode=true clean install
java -DdevMode=true -jar target/launchpad-backend-swarm.jar
```

Important: You MUST build the project with -DdevMode=true flag otherwise the necessary addons will be missing.

Build and Run the Unit Tests
----------------------------

* Execute:

        $ mvn clean install
        
Run the Integration Tests, Optionally Building
----------------------------------------------

* You need to set the following environment variables before running the integration tests: 

		$ export LAUNCHPAD_MISSIONCONTROL_SERVICE_HOST=localhost
		$ export LAUNCHPAD_MISSIONCONTROL_SERVICE_PORT=8080


* To build the project and run the integration tests, allowing Maven to start the WildFly Swarm server:
 
        $ mvn clean install -Pit


* To skip building and just run the integration tests, allowing Maven to start the WildFly Swarm server:

        $ mvn integration-test -Pit


