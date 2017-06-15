package io.openshift.launchpad.backend.rest;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 * Util class that creates a shrinkwrap deployment used to create IT tests.
 */
public class Deployments {

    /**
     * Create war archive to test.
     * @return the war used for testing
     */
    public static Archive<?> createDeployment()
    {
        List<String> packageNames = Arrays.asList(LaunchResource.class.getPackage().getName().split("\\."));
        String packageName = packageNames.stream()
                .filter(input -> packageNames.indexOf(input) != packageNames.size() - 1)
                .collect(Collectors.joining("."));
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        final File[] artifacts = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.jboss.forge:forge-service-core")
                .withTransitivity().asFile();
        deployment.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        deployment.merge(ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class)
                                 .importDirectory("target/launchpad-backend/WEB-INF/addons").as(GenericArchive.class),
                         "/WEB-INF/addons", Filters.include(".*"));
        deployment.addResource(LaunchResource.class);
        deployment.addResource(HealthResource.class);
        deployment.addPackages(true, packageName);
        deployment.addAsLibraries(artifacts);
        return deployment;
    }
}
