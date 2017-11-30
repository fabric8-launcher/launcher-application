package io.fabric8.launcher.service.openshift.impl;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.test.EnvironmentVariableController;
import io.fabric8.launcher.service.openshift.api.DuplicateProjectException;
import io.fabric8.launcher.service.openshift.api.OpenShiftCluster;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftEnvVarSysPropNames;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;
import io.fabric8.launcher.service.openshift.api.OpenShiftSettings;
import io.fabric8.launcher.service.openshift.impl.fabric8.openshift.client.Fabric8OpenShiftServiceImpl;
import io.fabric8.launcher.service.openshift.spi.OpenShiftServiceSpi;
import io.fabric8.launcher.service.openshift.test.OpenShiftTestCredentials;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de
 * Oliveira</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
@RunWith(Arquillian.class)
public class OpenShiftServiceIT {

    @Rule
    public DeleteOpenShiftProjectRule deleteOpenShiftProjectRule = new DeleteOpenShiftProjectRule(this);

    private static final Logger log = Logger.getLogger(OpenShiftServiceIT.class.getName());

    private static final String PREFIX_NAME_PROJECT = "test-project-";

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    private OpenShiftService openShiftService;

    /**
     * @return a jar file containing all the required classes to test the {@link OpenShiftService}
     */
    @Deployment
    public static WebArchive createDeployment() {
        // Import Maven runtime dependencies
        final File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
        // Create deploy file
        final WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackage(Fabric8OpenShiftServiceImpl.class.getPackage())
                .addPackage(OpenShiftServiceIT.class.getPackage())
                .addPackage(OpenShiftService.class.getPackage())
                .addClass(DeleteOpenShiftProjectRule.class)
                .addClass(OpenShiftServiceSpi.class)
                .addClass(OpenShiftTestCredentials.class)
                .addClasses(OpenShiftCluster.class, OpenShiftClusterRegistry.class, OpenShiftClusterRegistryImpl.class, OpenShiftClusterConstructor.class)
                .addAsResource("openshift-project-template.json")
                .addAsResource("foo-service-template.yaml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(dependencies);
        return war;
    }

    public OpenShiftService getOpenShiftService() {
        return this.openShiftServiceFactory.create(OpenShiftTestCredentials.getIdentity());
    }

    @Before
    public void setUp() {
        openShiftService = getOpenShiftService();
    }

    @Test
    public void createProjectOnly() {
        // given
        final String projectName = getUniqueProjectName();
        // when (just) creating the project
        final OpenShiftProject project = triggerCreateProject(projectName);
        // then
        final String actualName = project.getName();
        assertEquals("returned project did not have expected name", projectName, actualName);
    }

    @Test
    public void createProjectAndApplyTemplate() throws URISyntaxException, MalformedURLException {
        // given
        final String projectName = getUniqueProjectName();
        // when creating the project and then applying the template
        final OpenShiftProject project = triggerCreateProject(projectName);
        log.log(Level.INFO, "Created project: \'" + projectName + "\'");

        // TODO Issue #135 This reliance on tnozicka has to be cleared up,
        // introduced temporarily for testing as part of #134
        final URI projectGitHubRepoUri = new URI("https://github.com/redhat-kontinuity/jboss-eap-quickstarts.git");
        final URI pipelineTemplateUri = new URI(
                "https://raw.githubusercontent.com/redhat-kontinuity/jboss-eap-quickstarts/kontinu8/helloworld/.openshift-ci_cd/pipeline-template.yaml");
        final String gitRef = "kontinu8";

        openShiftService.configureProject(project, projectGitHubRepoUri, gitRef, pipelineTemplateUri);
        // then
        final String actualName = project.getName();
        assertEquals("returned project did not have expected name", projectName, actualName);
        assertThat(project.getResources()).isNotNull().hasSize(1);
        assertTrue(project.getResources().get(0).getKind().equals("BuildConfig"));
        assertEquals(openShiftService.getWebhookUrls(project).size(), 1);
        assertEquals(openShiftService.getWebhookUrls(project).get(0),
                     new URL(OpenShiftSettings.getOpenShiftConsoleUrl()
                                     + "/oapi/v1/namespaces/" + project.getName() + "/buildconfigs/helloworld-pipeline/webhooks/kontinu8/github"));
    }

    @Test(expected = DuplicateProjectException.class)
    public void duplicateProjectNameShouldFail() {
        // given
        final OpenShiftProject project = triggerCreateProject(getUniqueProjectName());
        // when
        final String name = project.getName();
        openShiftService.createProject(name);
        // then using same name should fail with DPE here
    }

    @Test
    public void projectExists() {
        // given
        final OpenShiftProject project = triggerCreateProject(getUniqueProjectName());
        // when
        final String name = project.getName();
        assertTrue(openShiftService.projectExists(name));
    }

    @Test(expected = IllegalArgumentException.class)
    public void projectExistsShouldFailIfNull() {
        openShiftService.projectExists(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void projectExistsShouldFailIfEmpty() {
        openShiftService.projectExists("");
    }


    @Test
    public void findProject() {
        // given
        String projectName = getUniqueProjectName();
        final OpenShiftProject project = triggerCreateProject(projectName);
        // when
        final String name = project.getName();
        assertTrue(openShiftService.findProject(name).isPresent());
    }

    @Test
    public void listProjects() {
        // given
        triggerCreateProject(getUniqueProjectName());

        // when
        List<OpenShiftProject> projects = openShiftService.listProjects();

        //then
        assertNotNull(projects);
        assertThat(projects).extracting(OpenShiftProject::getName).allMatch(s -> s.startsWith(PREFIX_NAME_PROJECT));
    }

    @Test
    public void findProjectWithInexistentName() {
        assertFalse(openShiftService.findProject("foo-project").isPresent());
    }


    @Test
    public void getServiceURL() throws Exception {
        // given
        OpenShiftProject openShiftProject = triggerCreateProject(getUniqueProjectName());
        InputStream serviceYamlFile = getClass().getClassLoader().getResourceAsStream("foo-service-template.yaml");
        openShiftService.configureProject(openShiftProject, serviceYamlFile, Collections.emptyMap());
        // when
        URL serviceURL = openShiftService.getServiceURL("foo", openShiftProject);
        //then
        assertNotNull(serviceURL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getServiceURLWithInexistentService() throws Exception {
        // given
        OpenShiftProject openShiftProject = triggerCreateProject(getUniqueProjectName());
        // when
        openShiftService.getServiceURL("foo", openShiftProject);
        //then
        fail("Should have thrown an exception");
    }

    @Test
    public void isDefaultIdentitySetWithToken() {
        String originalUserValue = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(OpenShiftEnvVarSysPropNames.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME);
        String originalPasswordValue = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(OpenShiftEnvVarSysPropNames.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD);
        String originalTokenValue = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(OpenShiftEnvVarSysPropNames.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN);

        try {
            EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN, "token");
            assertThat(openShiftServiceFactory.isDefaultIdentitySet()).isTrue();
            EnvironmentVariableController.removeEnv(OpenShiftEnvVarSysPropNames.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME);
            EnvironmentVariableController.removeEnv(OpenShiftEnvVarSysPropNames.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD);
            EnvironmentVariableController.removeEnv(OpenShiftEnvVarSysPropNames.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN);
            assertThat(openShiftServiceFactory.isDefaultIdentitySet()).isFalse();
        } finally {
            EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME, originalUserValue);
            EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD, originalPasswordValue);
            EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN, originalTokenValue);
        }
    }

    private String getUniqueProjectName() {
        return PREFIX_NAME_PROJECT + System.currentTimeMillis();
    }

    private OpenShiftProject triggerCreateProject(final String projectName) {
        final OpenShiftProject project = openShiftService.createProject(projectName);
        log.log(Level.INFO, "Created project: \'" + projectName + "\'");
        deleteOpenShiftProjectRule.add(project);
        return project;
    }
}
