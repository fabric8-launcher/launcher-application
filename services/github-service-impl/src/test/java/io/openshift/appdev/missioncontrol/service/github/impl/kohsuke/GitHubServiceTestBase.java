package io.openshift.appdev.missioncontrol.service.github.impl.kohsuke;

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

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import io.openshift.appdev.missioncontrol.service.github.api.DuplicateWebhookException;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubRepository;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubWebhook;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubWebhookEvent;
import io.openshift.appdev.missioncontrol.service.github.api.NoSuchRepositoryException;
import io.openshift.appdev.missioncontrol.service.github.api.NoSuchWebhookException;
import io.openshift.appdev.missioncontrol.service.github.spi.GitHubServiceSpi;
import io.openshift.appdev.missioncontrol.service.github.test.GitHubTestCredentials;

/**
 * Base test class for extension in Unit and Integration modes
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
abstract class GitHubServiceTestBase {

    private static final Logger log = Logger.getLogger(GitHubServiceTestBase.class.getName());

    private static final String NAME_GITHUB_SOURCE_REPO = "jboss-developer/jboss-eap-quickstarts";

    private static final String MY_GITHUB_SOURCE_REPO_PREFIX = "my-test-repo-";

    private static final String MY_GITHUB_REPO_DESCRIPTION = "Test project created by Arquillian.";

    private final List<String> repositoryNames = new ArrayList<>();

    @Before
    public void before() {
        this.repositoryNames.clear();
    }

    @After
    public void after() {
        repositoryNames.stream().map(repo -> GitHubTestCredentials.getUsername() + '/' + repo)
                .filter(repo -> ((GitHubServiceSpi) getGitHubService()).repositoryExists(repo))
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
        final GitHubRepository targetRepo = getGitHubService().fork(NAME_GITHUB_SOURCE_REPO);
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
    public void createGitHubRepositoryCannotBeNull() throws Exception {
        ((GitHubServiceSpi) getGitHubService()).createRepository(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryNameCannotBeNull() throws Exception {
        ((GitHubServiceSpi) getGitHubService()).createRepository(null, MY_GITHUB_REPO_DESCRIPTION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryDescriptionCannotBeNull() throws Exception {
        ((GitHubServiceSpi) getGitHubService()).createRepository(MY_GITHUB_SOURCE_REPO_PREFIX, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryCannotBeEmpty() throws Exception {
        ((GitHubServiceSpi) getGitHubService()).createRepository("", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryNameCannotBeEmpty() throws Exception {
        ((GitHubServiceSpi) getGitHubService()).createRepository("", MY_GITHUB_REPO_DESCRIPTION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryCannotDescriptionBeEmpty() throws Exception {
        ((GitHubServiceSpi) getGitHubService()).createRepository(MY_GITHUB_SOURCE_REPO_PREFIX, "");
    }

    @Test
    public void createGitHubRepository() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        // when
        final GitHubRepository targetRepo = ((GitHubServiceSpi) getGitHubService()).createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
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
        final GitHubRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        getGitHubService().push(targetRepo , tempDirectory.toFile());

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
        final GitHubRepository targetRepo = ((GitHubServiceSpi) getGitHubService()).createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        // when
        final GitHubWebhook webhook = getGitHubService().createWebhook(targetRepo, webhookUrl, GitHubWebhookEvent.ALL);
        // then
        Assert.assertNotNull(webhook);
        Assert.assertEquals(webhookUrl.toString(), webhook.getUrl());
    }

    @Test
    public void getGithubWebHook() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        final URL webhookUrl = new URL("https://10.1.2.2");
        final GitHubRepository targetRepo = ((GitHubServiceSpi) getGitHubService()).createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        // when
        final GitHubWebhook webhook = getGitHubService().createWebhook(targetRepo, webhookUrl, GitHubWebhookEvent.ALL);
        // then
        final GitHubWebhook roundtrip = ((GitHubServiceSpi) getGitHubService()).getWebhook(targetRepo, webhookUrl);
        Assert.assertNotNull("Could not get webhook we just created", roundtrip);
    }

    @Test(expected = NoSuchWebhookException.class)
    public void throwExceptionOnNoSuchWebhook() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        final URL fakeWebhookUrl = new URL("http://totallysomethingIMadeUp.com");
        final GitHubRepository targetRepo = ((GitHubServiceSpi) getGitHubService()).createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        // Try to get the webhook which does not exist.  Expect exception.
        ((GitHubServiceSpi) getGitHubService()).getWebhook(targetRepo, fakeWebhookUrl);
    }

    @Test(expected = DuplicateWebhookException.class)
    public void throwExceptionOnDuplicateWebhook() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        final URL webhookUrl = new URL("https://10.1.2.2");
        final GitHubRepository targetRepo = ((GitHubServiceSpi) getGitHubService()).createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        // Create the webhook.  Twice.  Expect exception
        getGitHubService().createWebhook(targetRepo, webhookUrl, GitHubWebhookEvent.ALL);
        getGitHubService().createWebhook(targetRepo, webhookUrl, GitHubWebhookEvent.ALL);
    }

    /**
     * @return The {@link GitHubService} used in testing
     */
    abstract GitHubService getGitHubService();

    private String generateRepositoryName() {
        final String repoName = MY_GITHUB_SOURCE_REPO_PREFIX + UUID.randomUUID().toString();
        this.repositoryNames.add(repoName);
        return repoName;
    }

}
