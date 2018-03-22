package io.fabric8.launcher.web;

import java.net.URI;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;

public class BaseResourceIT {

    @ArquillianResource
    protected URI deploymentUri;

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return Deployments.createDeployment();
    }
}
