package io.fabric8.launcher.service.openshift.impl;

import java.net.URL;
import java.util.logging.Logger;

import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases to ensure the {@link OpenShiftProjectImpl} is working as contracted
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class OpenShiftProjectImplTest {

    private static final Logger log = Logger.getLogger(OpenShiftProjectImplTest.class.getName());

    private static final String PROJECT_NAME = "test-name";

    private static OpenShiftProject project;

    @BeforeClass
    public static void initProject() throws Exception {
        project = new OpenShiftProjectImpl(PROJECT_NAME, new URL("http://localhost:8443"));
    }

    @Test
    public void name() {
        Assert.assertEquals(PROJECT_NAME, project.getName());
    }

    @Test
    public void consoleOverviewUrl() {
        final String expectedUrl = "http://localhost:8443" +
                "/console/project/" +
                PROJECT_NAME +
                "/overview/";
        log.info("Expected Console Overview URL: " + expectedUrl);
        final String actualUrl = project.getConsoleOverviewUrl().toExternalForm();
        log.info("Actual Console Overview URL: " + actualUrl);
        Assert.assertEquals(expectedUrl, actualUrl);
    }

}
