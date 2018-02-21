package io.fabric8.launcher.service.github.impl;

import io.fabric8.launcher.service.git.api.DuplicateHookException;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.NoSuchRepositoryException;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.api.GitHubWebhookEvent;
import io.fabric8.launcher.service.github.test.GitHubTestCredentials;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import static io.fabric8.launcher.service.hoverfly.HoverflyRuleConfigurer.createHoverflyProxy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Integration Tests for the {@link GitHubService}
 *
 * Relies on having environment variables set for:
 * GITHUB_USERNAME
 * GITHUB_TOKEN
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public final class GitHubServiceIT {

    private static final Logger log = Logger.getLogger(GitHubServiceIT.class.getName());

    private static final String NAME_GITHUB_SOURCE_REPO = "jboss-developer/jboss-eap-quickstarts";

    private static final String MY_GITHUB_REPO_DESCRIPTION = "Test project created by Arquillian.";

    @ClassRule
    public static RuleChain ruleChain = RuleChain
            .outerRule(new ProvideSystemProperty("https.proxyHost", "127.0.0.1")
                               .and("https.proxyPort", "8558")
                               .and("javax.net.ssl.trustStore", System.getenv("LAUNCHER_TESTS_TRUSTSTORE_PATH"))
                               .and("javax.net.ssl.trustStorePassword", "changeit"))
            .around(createHoverflyProxy("gh-simulation.json",
                                        "github.com|githubusercontent.com", 8558));

    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    private final List<String> repositoryNames = new ArrayList<>();

    @After
    public void after() {
        repositoryNames.stream()
                .filter(repo -> getGitHubService().getRepository(repo).isPresent())
                .forEach(repo -> ((GitServiceSpi) getGitHubService()).deleteRepository(repo));
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
        getGitHubService().createRepository(generateRepositoryName(), null);
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
        getGitHubService().createRepository(generateRepositoryName(), "");
    }

    @Test
    public void getLoggedUserIsReturned() {
        GitHubService service = getGitHubService();
        GitUser user = service.getLoggedUser();
        assertThat(user).isNotNull();
        // Relaxed condition as we use different accounts / organizations for actual GH calls - therefore simulation file might contain different username
        // In addition GET /user call is encrypted in the simulation file - making it harder to manipulate
        assertThat(user.getLogin()).isNotEmpty();
    }

    @Test
    public void createGitHubRepository() {
        // given
        final String repositoryName = generateRepositoryName();
        // when
        final GitRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        // then
        // Relaxed condition as we use different accounts / organizations for actual GH calls - therefore simulation file might contain different username
        assertThat(targetRepo.getFullName()).endsWith(repositoryName);
    }

    @Test
    public void createGitHubRepositoryWithContent() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        final Path tempDirectory = tmpFolder.getRoot().toPath();
        final Path file = tmpFolder.newFile("README.md").toPath();
        Files.write(file, Collections.singletonList("Read me to know more"), Charset.forName("UTF-8"));

        // when
        final GitRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        getGitHubService().push(targetRepo, tempDirectory);

        // then
        URI readmeUri = URI.create("https://raw.githubusercontent.com/" + GitHubTestCredentials.getUsername() + "/" + repositoryName + "/master/README.md");
        HttpURLConnection connection = (HttpURLConnection) readmeUri.toURL().openConnection();
        assertThat(connection.getResponseCode()).describedAs("README.md should have been pushed to the repo").isEqualTo(200);
    }

    @Test
    public void createGithubWebHook() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        final URL webhookUrl = new URL("https://10.1.2.2");
        final GitRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        // when
        final GitHook webhook = getGitHubService().createHook(targetRepo, null, webhookUrl, GitHubWebhookEvent.ALL.name());
        // then
        Assert.assertNotNull(webhook);
        Assert.assertEquals(webhookUrl.toString(), webhook.getUrl());
    }

    @Test
    public void createGithubWebHookWithSecret() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        final URL webhookUrl = new URL("https://10.1.2.2");
        final GitRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        // when
        final GitHook webhook = getGitHubService().createHook(targetRepo, "my secret", webhookUrl, GitHubWebhookEvent.ALL.name());
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
        final GitHook webhook = getGitHubService().createHook(targetRepo, "my secret", webhookUrl, GitHubWebhookEvent.ALL.name());
        // then
        final Optional<GitHook> roundtrip = getGitHubService().getHook(targetRepo, webhookUrl);
        Assert.assertNotNull("Could not get webhook we just created", roundtrip);
    }

    @Test
    public void throwExceptionOnNoSuchWebhook() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        final URL fakeWebhookUrl = new URL("http://totallysomethingIMadeUp.com");
        final GitRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);

        assertThat(getGitHubService().getHook(targetRepo, fakeWebhookUrl)).isNotPresent();
    }

    @Test
    public void throwExceptionOnDuplicateWebhook() throws Exception {
        // given
        final String repositoryName = generateRepositoryName();
        final URL webhookUrl = new URL("https://10.1.2.2");
        final GitRepository targetRepo = getGitHubService().createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);

        // Create the webhook.  Twice.  Expect exception
        getGitHubService().createHook(targetRepo, null, webhookUrl, GitHubWebhookEvent.ALL.name());
        assertThatExceptionOfType(DuplicateHookException.class).isThrownBy(() -> getGitHubService().createHook(targetRepo, null, webhookUrl, GitHubWebhookEvent.ALL.name()))
                .withMessageContaining("Could not create webhook as it already exists");
    }

    private GitHubService getGitHubService() {
        return new KohsukeGitHubServiceFactoryImpl().create(GitHubTestCredentials.getToken());
    }

    // - Generating repo per test method

    @Rule
    public final TestName testName = new TestName();

    private String generateRepositoryName() {
        final String repoName = this.getClass().getSimpleName() + "-" + testName.getMethodName();
        this.repositoryNames.add(repoName);
        return repoName;
    }

    @Test
    @Ignore("Fix hoverfly mapping")
    public void readOrganizations() {
        List<GitOrganization> organizations = getGitHubService().getOrganizations();
        assertThat(organizations).isNotNull();
    }

    @Test
    @Ignore("Fix hoverfly mapping")
    public void readRepositories() {
        List<GitRepository> repos = getGitHubService().getRepositories(null);
        assertThat(repos).isNotNull();
    }
}
