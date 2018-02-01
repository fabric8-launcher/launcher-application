package io.fabric8.launcher.web;

import java.net.URI;

import io.fabric8.launcher.web.api.Deployments;
import org.arquillian.smart.testing.rules.git.server.GitServer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.ClassRule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class BaseResourceIT {

    @ClassRule
    public static final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @ClassRule
    public static GitServer gitServer = GitServer.bundlesFromDirectory("repos/boosters")
            .usingPort(8765)
            .create();

    static {
        environmentVariables.set("LAUNCHER_BOOSTER_CATALOG_REPOSITORY", "http://localhost:8765/booster-catalog");
        environmentVariables.set("LAUNCHER_GIT_HOST", "http://localhost:8765/");
        environmentVariables.set("JENKINSFILE_LIBRARY_GIT_REPOSITORY", "http://localhost:8765/fabric8-jenkinsfile-library/");
    }

    @ArquillianResource
    protected URI deploymentUri;

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return Deployments.createDeployment();
    }
}
