# Code Generator Backend

[![Build Status](https://ci.centos.org/job/devtools-generator-backend-build-master/badge/icon)](https://ci.centos.org/job/devtools-generator-backend-build-master)

This code generator project which is a Java backend system exposes several JBoss Forge commands
using a REST endpoint. The backend runs within a WildFly Swarm container and is called from
an Angularjs 2 Front application responsible to collect from a end user the information needed to generate
a Zip file containing an Apache Maven project populated for an Eclipse Vert.x, Spring Boot or WildFly Swarm
container.

To execute this project simply do a maven build:

```bash
$ mvn package -s configuration/settings.xml
```

Remark : This project requires that you compile this [github project](http://github.com/obsidian-toaster/obsidian-addon) hosting the Obsidian JBoss Addon.

And then execute the fat-jar in the target folder with:

```bash
$ java -jar target/generator-swarm.jar
```

Then follow the [front-end ReadMe][1] to run the front-end.

[1]:https://github.com/obsidian-toaster/generator-frontend/blob/master/README.md

## Development mode

Run with the `-DdevMode=true` flag to auto-reload SNAPSHOT addons that are installed in your local maven repository. The changes will last as long as the container is alive.
Make sure to rebuild the backend if for some reason you need to stop the container:
```
java -DdevMode=true -jar target/generator-swarm.jar
```

Build and Run the Unit Tests
----------------------------

* Execute:

        $ mvn clean install
        
Run the Integration Tests, Optionally Building
----------------------------------------------

* You need to set the CATAPULT_URL environment before running the integration tests: 

		$ export CATAPULT_URL=http://localhost:8080


* To build the project and run the integration tests, allowing Maven to start the WildFly Swarm server:
 
        $ mvn clean install -Pit


* To skip building and just run the integration tests, allowing Maven to start the WildFly Swarm server:

        $ mvn integration-test -Pit