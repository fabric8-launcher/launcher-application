# Code Generator Backend

[![Build Status](https://travis-ci.org/obsidian-toaster/generator-backend.svg?branch=master)](https://travis-ci.org/obsidian-toaster/generator-backend)

This code generator project which is a Java backend system exposes several JBoss Forge commands
using a REST endpoint. The backend runs within a WildFly Swarm container and is called from 
an Angularjs 2 Front application responsible to collect from a end user the information needed to generate 
a Zip file containing an Apache Maven project populated for an Eclipse Vert.x, Spring Boot or WildFly Swarm 
container. 

To execute this project simply do a maven build:

```bash
$ mvn package
```

Remark : This project requires that you compile this [github project](http://github.com/obsidian-toaster/obsidian-addon) hosting the Obsidian JBoss Addon.

And then execute the fat-jar in the target folder with:

```bash
$ java -jar target/generator-swarm.jar 
```

Then follow the [front-end ReadMe][1] to run the front-end.

[1]:https://github.com/obsidian-toaster/generator-frontend/blob/master/README.md
