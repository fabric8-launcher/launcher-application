package org.jboss.obsidian.generator.rest;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
@RunWith(Arquillian.class)
public class ObsidianResourceTest {

    @Deployment
    public static Archive createDeployment() {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        final File[] artifacts = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.jboss.forge.addon:ui-api")
                .withTransitivity().asFile();
        deployment.addResource(ObsidianResource.class);
        deployment.addPackages(true, "org.jboss.obsidian.generator");
        deployment.addAsLibraries(artifacts);
        return deployment;
    }

    @ArquillianResource
    private URI deploymentUri;

    @Test
    @RunAsClient
    public void simpleGet() {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(UriBuilder.fromUri(deploymentUri).path("forge"));
        final Response result = webTarget.path("/version").request().get();
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }
}