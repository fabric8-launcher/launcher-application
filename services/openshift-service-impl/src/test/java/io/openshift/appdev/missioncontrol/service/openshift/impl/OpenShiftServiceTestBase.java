package io.openshift.appdev.missioncontrol.service.openshift.impl;


import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.openshift.appdev.missioncontrol.service.openshift.api.DuplicateProjectException;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftSettings;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
public abstract class OpenShiftServiceTestBase implements OpenShiftServiceContainer {

    @Rule
    public DeleteOpenShiftProjectRule deleteOpenShiftProjectRule = new DeleteOpenShiftProjectRule(this);

    private static final Logger log = Logger.getLogger(OpenShiftServiceTestBase.class.getName());

    private static final String PREFIX_NAME_PROJECT = "test-project-";

    private OpenShiftService openShiftService;

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
        Assertions.assertThat(project.getResources()).isNotNull().hasSize(1);
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
        assertEquals(1, projects.size());
        assertTrue(projects.get(0).getName().startsWith(PREFIX_NAME_PROJECT));
    }

    @Test
    public void findProjectWithInexistentName() {
        assertFalse(openShiftService.findProject("foo-project").isPresent());
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
