package io.fabric8.launcher.service.openshift.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.fabric8.launcher.base.test.EnvironmentVariableController;
import io.fabric8.launcher.service.openshift.api.DuplicateProjectException;
import io.fabric8.launcher.service.openshift.api.OpenShiftEnvVarSysPropNames;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de
 * Oliveira</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
public class OpenShiftServiceIT {

    @Rule
    public DeleteOpenShiftProjectRule deleteOpenShiftProjectRule = new DeleteOpenShiftProjectRule(this);

    private static final Logger log = Logger.getLogger(OpenShiftServiceIT.class.getName());

    private static final String PREFIX_NAME_PROJECT = "test-project-";

    private OpenShiftServiceFactory openShiftServiceFactory;

    private OpenShiftService openShiftService;

    @Before
    public void setUp() {
        this.openShiftServiceFactory = new Fabric8OpenShiftServiceFactory(new OpenShiftClusterRegistryImpl());
        this.openShiftService = openShiftServiceFactory.create();
    }


    OpenShiftService getOpenShiftService() {
        return openShiftService;
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
    public void createProjectAndApplyTemplate() throws IOException {
        // given
        final String projectName = getUniqueProjectName();

        // when creating the project and then applying the template
        final OpenShiftProject project = triggerCreateProject(projectName);
        log.log(Level.INFO, "Created project: \'" + projectName + "\'");

        try (InputStream template = getClass().getResourceAsStream("/foo-pipeline-template.yaml")) {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("GIT_URL", "https://foo.com/blah");
            parameters.put("GIT_REF", "kontinu8");
            openShiftService.configureProject(project, template, parameters);
        }

        // then
        final String actualName = project.getName();
        assertThat(actualName).isEqualTo(projectName);
        assertThat(project.getResources()).isNotNull().hasSize(1);
        assertThat(project.getResources().get(0).getKind()).isEqualTo("BuildConfig");
        assertThat(openShiftService.getWebhookUrls(project)).hasSize(1);
        assertThat(openShiftService.getWebhookUrls(project).get(0)).isEqualTo(
                new URL(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL.value()
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
    public void findProjectWithNonExistingName() {
        assertThat(openShiftService.findProject("foo-project")).isNotPresent();
    }

    @Test
    public void getServiceURL() {
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
    public void getServiceURLWithInexistentService() {
        OpenShiftProject openShiftProject = triggerCreateProject(getUniqueProjectName());
        assertThatThrownBy(() -> openShiftService.getServiceURL("foo", openShiftProject)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void isDefaultIdentitySetWithToken() {
        String originalUserValue = OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME.value();
        String originalPasswordValue = OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD.value();
        String originalTokenValue = OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN.value();

        try {
            EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN.propertyKey(), "token");
            assertThat(openShiftServiceFactory.getDefaultIdentity()).isPresent();
            EnvironmentVariableController.removeEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME.propertyKey());
            EnvironmentVariableController.removeEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD.propertyKey());
            EnvironmentVariableController.removeEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN.propertyKey());
            assertThat(openShiftServiceFactory.getDefaultIdentity()).isNotPresent();
        } finally {
            EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME.propertyKey(), originalUserValue);
            EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD.propertyKey(), originalPasswordValue);
            EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN.propertyKey(), originalTokenValue);
        }
    }

    @Test
    public void openShiftClientIsNotNull() {
        assertThat(openShiftService.getOpenShiftClient()).isNotNull();
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
