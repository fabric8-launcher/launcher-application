package io.openshift.appdev.missioncontrol.core.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.io.Files;

import io.openshift.appdev.missioncontrol.core.api.Boom;
import io.openshift.appdev.missioncontrol.core.api.CreateProjectile;
import io.openshift.appdev.missioncontrol.core.api.ForkProjectile;
import io.openshift.appdev.missioncontrol.core.api.MissionControl;
import io.openshift.appdev.missioncontrol.core.api.ProjectileBuilder;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubRepository;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubServiceFactory;
import io.openshift.appdev.missioncontrol.service.github.api.NoSuchRepositoryException;
import io.openshift.appdev.missioncontrol.service.github.spi.GitHubServiceSpi;
import io.openshift.appdev.missioncontrol.service.github.test.GitHubTestCredentials;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftServiceFactory;
import io.openshift.appdev.missioncontrol.service.openshift.spi.OpenShiftServiceSpi;
import io.openshift.appdev.missioncontrol.service.openshift.test.OpenShiftTestCredentials;

/**
 * Test cases for the {@link MissionControl}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
@RunWith(Arquillian.class)
public class MissionControlIT {

    private static final Logger log = Logger.getLogger(MissionControlIT.class.getName());

    //TODO #135 Remove reliance on tzonicka
    private static final String GITHUB_SOURCE_REPO_NAME = "jboss-eap-quickstarts";

    private static final String GITHUB_SOURCE_REPO_FULLNAME = "redhat-kontinuity/" + GITHUB_SOURCE_REPO_NAME;

    private static final String GIT_REF = "kontinu8";

    private static final String PIPELINE_TEMPLATE_PATH = "helloworld/.openshift-ci_cd/pipeline-template.yaml";

    private static final String PREFIX_NAME_PROJECT = "test-project-";

    private final Collection<String> openshiftProjectsToDelete = new ArrayList<>();

    private final Collection<String> githubReposToDelete = new ArrayList<>();

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    private GitHubServiceFactory gitHubServiceFactory;

    @Inject
    private MissionControl missionControl;

    /**
     * @return a ear file containing all the required classes and dependencies
     * to test the {@link MissionControl}
     */
    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        // Import Maven runtime dependencies
        final File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
        // Create deploy file
        final WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackage(MissionControl.class.getPackage())
                .addPackage(MissionControlImpl.class.getPackage())
                .addPackage(GitHubTestCredentials.class.getPackage())
                .addAsWebInfResource("META-INF/jboss-deployment-structure.xml", "jboss-deployment-structure.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(dependencies);
        // Show the deployed structure
        log.info(war.toString(true));
        return war;
    }

    @Before
    @After
    public void cleanupGitHubProjects() {
        // also, make sure that the GitHub user's account does not already contain the repo to fork
        // After the test remove the repository we created
        final String repositoryName = GitHubTestCredentials.getUsername() + "/" + GITHUB_SOURCE_REPO_NAME;
        final GitHubService gitHubService = gitHubServiceFactory.create(GitHubTestCredentials.getToken());
        try {
            ((GitHubServiceSpi) gitHubService).deleteRepository(repositoryName);
        } catch (NoSuchRepositoryException e) {
            // ignore
            log.info("Repository '" + repositoryName + "' does not exist.");
        }
        githubReposToDelete.forEach(repoName -> {
            final String fullRepoName = GitHubTestCredentials.getUsername() + '/' + repoName;
            try {
                ((GitHubServiceSpi) gitHubService).deleteRepository(fullRepoName);
                log.info("Deleted GitHub repository: " + fullRepoName);
            } catch (final NoSuchRepositoryException nsre) {
                log.severe("Could not remove GitHub repo " + fullRepoName + ": " + nsre.getMessage());
            }

        });
        githubReposToDelete.clear();
    }

    @Before
    @After
    public void cleanupOpenShiftProjects() {
        OpenShiftService openShiftService = openShiftServiceFactory.create(OpenShiftTestCredentials.getIdentity());
        openshiftProjectsToDelete.forEach(projectName -> {
            final boolean deleted = ((OpenShiftServiceSpi) openShiftService).deleteProject(projectName);
            if (deleted) {
                log.info("Deleted OpenShift project: " + projectName);
            }
        });
        openshiftProjectsToDelete.clear();
    }

    @Test
    public void flingFork() {
        // Define the projectile with a custom, unique OpenShift project name.
        final String expectedName = getUniqueProjectName();
        final ForkProjectile projectile = ProjectileBuilder.newInstance()
                .gitHubIdentity(GitHubTestCredentials.getToken())
                .openShiftIdentity(OpenShiftTestCredentials.getIdentity())
                .openShiftProjectName(expectedName)
                .forkType()
                .sourceGitHubRepo(GITHUB_SOURCE_REPO_FULLNAME)
                .gitRef(GIT_REF)
                .pipelineTemplatePath(PIPELINE_TEMPLATE_PATH)
                .build();

        // Fling
        final Boom boom = missionControl.launch(projectile);

        // Assertions
        assertions(expectedName, boom);
    }

    @Test
    public void flingCreate() {
        // Define the projectile with a custom, unique OpenShift project name.
        final String expectedName = getUniqueProjectName();
        File tempDir = Files.createTempDir();
        final CreateProjectile projectile = ProjectileBuilder.newInstance()
                .gitHubIdentity(GitHubTestCredentials.getToken())
                .openShiftIdentity(OpenShiftTestCredentials.getIdentity())
                .openShiftProjectName(expectedName)
                .createType()
                .projectLocation(tempDir.toPath())
                .build();

        // Mark GitHub repo for deletion
        githubReposToDelete.add(expectedName);

        // Fling
        final Boom boom = missionControl.launch(projectile);

        // Assertions
        assertions(expectedName, boom);
    }

    private void assertions(String expectedName, Boom boom) {
        /*
           Can't really assert on any of the properties of the
           new repo because they could change in GitHub and
           break our tests
         */
        final GitHubRepository createdRepo = boom.getCreatedRepository();
        Assert.assertNotNull("repo can not be null", createdRepo);
        final OpenShiftProject createdProject = boom.getCreatedProject();
        Assert.assertNotNull("project can not be null", createdProject);
        final String foundName = createdProject.getName();
        log.info("Created OpenShift project: " + foundName);
        openshiftProjectsToDelete.add(foundName);
        Assert.assertEquals(expectedName, foundName);
        // checking that the Build Config and the ImageStream were created.
        Assertions.assertThat(createdProject.getResources()).isNotNull().hasSize(2);
        assertTrue(createdProject.getResources().get(0).getKind().equals("ImageStream"));
        assertTrue(createdProject.getResources().get(1).getKind().equals("BuildConfig"));
        assertFalse(boom.getGitHubWebhooks().isEmpty());
    }

    private String getUniqueProjectName() {
        return PREFIX_NAME_PROJECT + System.currentTimeMillis();
    }
}
