package io.fabric8.launcher.service.openshift.impl;

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

import javax.inject.Inject;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Deployment
    public static WebArchive createDeployment() {
        // Import Maven runtime dependencies
        final File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
        // Create deploy file
        final WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackages(false, Fabric8OpenShiftServiceImpl.class.getPackage(), OpenShiftServiceIT.class.getPackage(), OpenShiftService.class.getPackage())
                .addClasses(DeleteOpenShiftProjectRule.class, OpenShiftServiceSpi.class, OpenShiftTestCredentials.class)
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
        assertThat(actualName).isEqualTo(projectName);
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
        assertThat(actualName).isEqualTo(projectName);
        assertThat(project.getResources()).isNotNull().hasSize(1);
        assertThat(project.getResources().get(0).getKind()).isEqualTo("BuildConfig");
        assertThat(openShiftService.getWebhookUrls(project)).hasSize(1);
        assertThat(openShiftService.getWebhookUrls(project).get(0)).isEqualTo(
                     new URL(OpenShiftSettings.getOpenShiftConsoleUrl()
                                     + "/oapi/v1/namespaces/" + project.getName() + "/buildconfigs/helloworld-pipeline/webhooks/kontinu8/github"));
    }

    @Test
    public void duplicateProjectNameShouldFail() {
        // given
        final OpenShiftProject project = triggerCreateProject(getUniqueProjectName());
        // when
        final String name = project.getName();
        assertThatThrownBy(() -> openShiftService.createProject(name)).isInstanceOf(DuplicateProjectException.class);

        // then using same name should fail with DPE here
    }

    @Test
    public void projectExists() {
        // given
        final OpenShiftProject project = triggerCreateProject(getUniqueProjectName());
        // when
        final String name = project.getName();

        // then
        assertThat(openShiftService.projectExists(name)).isTrue();
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
        assertThat(openShiftService.findProject(name)).isPresent();
    }

    @Test
    public void listProjects() {
        // given
        final String uniqueProjectName = getUniqueProjectName();
        triggerCreateProject(uniqueProjectName);

        // when
        List<OpenShiftProject> projects = openShiftService.listProjects();

        // then
        assertThat(projects).extracting(OpenShiftProject::getName).contains(uniqueProjectName);
    }

    @Test
    public void findProjectWithNonExistingName() {
        assertThat(openShiftService.findProject("foo-project")).isNotPresent();
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
        assertThat(serviceURL).isNotNull();
    }

    @Test
    public void getServiceURLWithInexistentService() throws Exception {
        OpenShiftProject openShiftProject = triggerCreateProject(getUniqueProjectName());
        assertThatThrownBy(() -> openShiftService.getServiceURL("foo", openShiftProject)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void isDefaultIdentitySetWithToken() {
        String originalUserValue = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME);
        String originalPasswordValue = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD);
        String originalTokenValue = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN);

        try {
            EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN, "token");
            assertThat(openShiftServiceFactory.getDefaultIdentity()).isPresent();
            EnvironmentVariableController.removeEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME);
            EnvironmentVariableController.removeEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD);
            EnvironmentVariableController.removeEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN);
            assertThat(openShiftServiceFactory.getDefaultIdentity()).isNotPresent();
        } finally {
            EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME, originalUserValue);
            EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD, originalPasswordValue);
            EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN, originalTokenValue);
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
