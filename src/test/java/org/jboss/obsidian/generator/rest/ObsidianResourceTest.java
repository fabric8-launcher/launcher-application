package org.jboss.obsidian.generator.rest;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertNotNull;

/**
 *
 */
@RunWith(Arquillian.class)
public class ObsidianResourceTest {

    @Deployment
    public static Archive createDeployment() {
        JAXRSArchive deployment = ShrinkWrap.create( JAXRSArchive.class );
        deployment.addResource( ObsidianResource.class );
        return deployment;
    }

    @Test
    public void simpleGet(@ArquillianResteasyResource WebTarget webTarget)
    {
        final Response result = webTarget.path("/").request().get();

        assertNotNull(result);

    }
}