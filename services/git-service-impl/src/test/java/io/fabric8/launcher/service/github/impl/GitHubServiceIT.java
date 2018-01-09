package io.fabric8.launcher.service.github.impl;


import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import io.fabric8.launcher.service.git.api.DuplicateHookException;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.NoSuchHookException;
import io.fabric8.launcher.service.git.api.NoSuchRepositoryException;
import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import io.fabric8.launcher.service.github.api.GitHubWebhookEvent;
import io.fabric8.launcher.service.github.spi.GitHubServiceSpi;
import io.fabric8.launcher.service.github.test.GitHubTestCredentials;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE;
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME;

/**
 * Integration Tests for the {@link GitHubService}
 *
 * Relies on having environment variables set for:
 * GITHUB_USERNAME
 * GITHUB_TOKEN
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@RunWith(Arquillian.class)
public final class GitHubServiceIT {

    private static final Logger log = Logger.getLogger(GitHubServiceIT.class.getName());

    private static final String NAME_GITHUB_SOURCE_REPO = "jboss-developer/jboss-eap-quickstarts";

    private static final String MY_GITHUB_SOURCE_REPO_PREFIX = "my-test-repo-";

    private static final String MY_GITHUB_REPO_DESCRIPTION = "Test project created by Arquillian.";

    private final List<String> repositoryNames = new ArrayList<>();

    @Inject
    private GitHubServiceFactory gitHubServiceFactory;

    /**
     * @return a war file containing all the required classes and dependencies
     * to test the {@link GitHubService}
     */
    @Deployment
    public static WebArchive createDeployment() {
        // Import Maven runtime dependencies
        final File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .importDependencies(RUNTIME, COMPILE)
                .resolve().withTransitivity()
                .asFile();

        final File[] testDependencies = Maven.resolver().loadPomFromFile("pom.xml").resolve("org.assertj:assertj-core").withoutTransitivity().asFile();
        // Create deploy file
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackage(KohsukeGitHubServiceFactoryImpl.class.getPackage())
                .addClass(GitHubTestCredentials.class)
                .addClass(GitHubServiceSpi.class)
                // libraries will include all classes/interfaces from the API project.
                .addAsLibraries(dependencies)
                .addAsLibraries(testDependencies);
        // Show the deployed structure
        log.fine(war.toString(true));
        return war;
    }


    @Before
    public void before() {
        this.repositoryNames.clear();
    }

    @After
    public void after() {
        repositoryNames.stream().map(repo -> GitHubTestCredentials.getUsername() + '/' + repo)
                .filter(repo -> getGitHubService().repositoryExists(repo))
                .forEach(repo -> ((GitHubServiceSpi) getGitHubService()).deleteRepository(repo));
    }

    @Test(expected = IllegalArgumentException.class)
    public void forkRepoCannotBeNull() {
        getGitHubService().fork(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void forkRepoCannotBeEmpty() {
        getGitHubService().fork("");
    }

    @Test
    public void fork() {
        // when
        final GitRepository targetRepo = getGitHubService().fork(NAME_GITHUB_SOURCE_REPO);
        // then
        Assert.assertNotNull("Got null result in forking " + NAME_GITHUB_SOURCE_REPO, targetRepo);
        log.log(Level.INFO, "Forked " + NAME_GITHUB_SOURCE_REPO + " as " + targetRepo.getFullName() + " available at "
                + targetRepo.getHomepage());
    }

    @Test(expected = NoSuchRepositoryException.class)
    public void cannotForkNonexistentRepo() {
        getGitHubService().fork("ALRubinger/someRepoThatDoesNotAndWillNeverExist");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryCannotBeNull() {
        getGitHubService().createRepository(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryNameCannotBeNull() {
        getGitHubService().createRepository(null, MY_GITHUB_REPO_DESCRIPTION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryDescriptionCannotBeNull() {
        getGitHubService().createRepository(MY_GITHUB_SOURCE_REPO_PREFIX, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryCannotBeEmpty() {
        getGitHubService().createRepository("", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryNameCannotBeEmpty() {
        getGitHubService().createRepository("", MY_GITHUB_REPO_DESCRIPTION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryCannotDescriptionBeEmpty() {
        getGitHubService().createRepository(MY_GITHUB_SOURCE_REPO_PREFIX, "");
    }

    @Test
    public void createGitHubRepository() {
        // given
        final String repositoryName = generateRepositoryName();
        // when
        final GitRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        // then
        Assert.assertEquals(GitHubTestCredentials.getUsername() + "/" + repositoryName, targetRepo.getFullName());
    }

    @Test
    public void createGitHubRepositoryWithContent() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        Path tempDirectory = Files.createTempDirectory("test");
        Path file = tempDirectory.resolve("README.md");
        Files.write(file, Collections.singletonList("Read me to know more"), Charset.forName("UTF-8"));

        // when
        final GitRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        getGitHubService().push(targetRepo, tempDirectory.toFile());

        // then
        Assert.assertEquals(GitHubTestCredentials.getUsername() + "/" + repositoryName, targetRepo.getFullName());
        URI readmeUri = UriBuilder.fromUri("https://raw.githubusercontent.com/")
                .path(GitHubTestCredentials.getUsername())
                .path(repositoryName)
                .path("/master/README.md").build();
        HttpURLConnection connection = (HttpURLConnection) readmeUri.toURL().openConnection();
        Assert.assertEquals("README.md should have been pushed to the repo", 200, connection.getResponseCode());

        FileUtils.forceDelete(tempDirectory.toFile());
    }

    @Test
    public void createGithubWebHook() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        final URL webhookUrl = new URL("https://10.1.2.2");
        final GitRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        // when
        final GitHook webhook = getGitHubService().createHook(targetRepo, webhookUrl, GitHubWebhookEvent.ALL);
        // then
        Assert.assertNotNull(webhook);
        Assert.assertEquals(webhookUrl.toString(), webhook.getUrl());
    }

    @Test
    public void getGithubWebHook() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        final URL webhookUrl = new URL("https://10.1.2.2");
        final GitRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        // when
        final GitHook webhook = getGitHubService().createHook(targetRepo, webhookUrl, GitHubWebhookEvent.ALL);
        // then
        final GitHook roundtrip = ((GitHubServiceSpi) getGitHubService()).getWebhook(targetRepo, webhookUrl);
        Assert.assertNotNull("Could not get webhook we just created", roundtrip);
    }

    @Test
    public void throwExceptionOnNoSuchWebhook() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        final URL fakeWebhookUrl = new URL("http://totallysomethingIMadeUp.com");
        final GitRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);

        assertThatExceptionOfType(NoSuchHookException.class).isThrownBy(() -> ((GitHubServiceSpi) getGitHubService()).getWebhook(targetRepo, fakeWebhookUrl));
    }

    @Test
    public void throwExceptionOnDuplicateWebhook() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        final URL webhookUrl = new URL("https://10.1.2.2");
        final GitRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);

        // Create the webhook.  Twice.  Expect exception
        getGitHubService().createHook(targetRepo, webhookUrl, GitHubWebhookEvent.ALL);
        assertThatExceptionOfType(DuplicateHookException.class).isThrownBy(() -> getGitHubService().createHook(targetRepo, webhookUrl, GitHubWebhookEvent.ALL));
    }

    private String generateRepositoryName() {
        final String repoName = MY_GITHUB_SOURCE_REPO_PREFIX + UUID.randomUUID().toString();
        this.repositoryNames.add(repoName);
        return repoName;
    }


    private GitHubService getGitHubService() {
        return gitHubServiceFactory.create(GitHubTestCredentials.getToken());
    }
}
