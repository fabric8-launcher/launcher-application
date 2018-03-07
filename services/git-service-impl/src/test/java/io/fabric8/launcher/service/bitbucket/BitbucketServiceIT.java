package io.fabric8.launcher.service.bitbucket;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.fabric8.launcher.service.bitbucket.api.BitbucketWebhookEvent;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createHoverflyProxy;
import static io.fabric8.launcher.service.bitbucket.api.BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD;
import static io.fabric8.launcher.service.bitbucket.api.BitbucketEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME;

public class BitbucketServiceIT {
    private static final String MY_BITBUCKET_REPO_DESCRIPTION = "Test project created by Integration Tests.";

    @ClassRule
    public static RuleChain ruleChain = RuleChain
            // After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment()
                    .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_BITBUCKET_USERNAME, "bbUser")
                    .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_BITBUCKET_APPLICATION_PASSWORD, "enfjaj2RE3R3JNF"))
            .around(createHoverflyProxy("bb-simulation.json", "api.bitbucket.org"));

    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    @Rule
    public final TestName testName = new TestName();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private BitbucketService bitbucketService = new BitbucketServiceFactory().create();

    private List<GitRepository> repositoriesToDelete = new ArrayList<>();

    @Test
    public void gitUserIsReturned() {
        GitUser user = bitbucketService.getLoggedUser();
        softly.assertThat(user).isNotNull();
        // Relaxed condition as we use different accounts / organizations for actual GL calls - therefore simulation file might contain different username
        softly.assertThat(user.getLogin()).isNotEmpty();
    }

    @Test
    public void repositoryDoesNotExist() {
        Optional<GitRepository> repo = bitbucketService.getRepository("RepositoryDoesNotExist");
        softly.assertThat(repo).isNotPresent();
    }

    @Test
    public void repositoryExists() {
        final GitRepository repo = createRepository();
        Optional<GitRepository> repository = bitbucketService.getRepository(repo.getFullName());
        softly.assertThat(repository).isPresent();
    }

    @Test
    public void shouldCreateRepository() {
        GitRepository repo = createRepository();
        softly.assertThat(repo).isNotNull();
        Optional<GitRepository> repository = bitbucketService.getRepository(repo.getFullName());
        softly.assertThat(repository).isPresent();
    }

    @Test
    public void createHook() throws Exception {
        GitRepository repo = createRepository();
        GitHook hook = bitbucketService.createHook(repo, null, new URL("http://www.google.com"),
                BitbucketWebhookEvent.REPO_PUSH.id(), BitbucketWebhookEvent.PULL_REQUEST_CREATED.id());
        softly.assertThat(hook).isNotNull();
        softly.assertThat(hook.getName()).isNotEmpty();
        softly.assertThat(hook.getUrl()).isEqualTo("http://www.google.com");
        softly.assertThat(hook.getEvents()).containsExactlyInAnyOrder(BitbucketWebhookEvent.REPO_PUSH.id(), BitbucketWebhookEvent.PULL_REQUEST_CREATED.id());
    }

    @Test
    public void deleteHook() throws Exception {
        GitRepository repo = createRepository();
        GitHook hook = bitbucketService.createHook(repo, null, new URL("http://www.google.com"),
                BitbucketWebhookEvent.REPO_PUSH.id(), BitbucketWebhookEvent.PULL_REQUEST_CREATED.id());
        bitbucketService.deleteWebhook(repo, hook);
        Optional<GitHook> deletedHook = bitbucketService.getHook(repo, new URL(hook.getUrl()));
        softly.assertThat(deletedHook).isNotPresent();
    }

    @Test
    public void readOrganizations() {
        List<GitOrganization> organizations = bitbucketService.getOrganizations();
        softly.assertThat(organizations).isNotNull();
    }

    @Test
    public void readRepositoriesWithOrganization() {
        List<GitRepository> repos = bitbucketService.getRepositories(ImmutableGitOrganization.of("Brozap"));
        softly.assertThat(repos).isNotNull();
    }

    @Test
    public void readRepositories() {
        List<GitRepository> repos = bitbucketService.getRepositories(null);
        softly.assertThat(repos).isNotNull();
    }

    @After
    public void tearDown() {
        for (GitRepository repo : repositoriesToDelete) {
            ((GitServiceSpi) bitbucketService).deleteRepository(repo);
        }
        repositoriesToDelete.clear();
    }

    // - Generating repo per test method
    private GitRepository createRepository() {
        GitRepository repository = bitbucketService.createRepository(generateRepositoryName(), MY_BITBUCKET_REPO_DESCRIPTION);
        repositoriesToDelete.add(repository);
        return repository;
    }

    private String generateRepositoryName() {
        return this.getClass().getSimpleName().toLowerCase() + "-" + testName.getMethodName().toLowerCase();
    }
}
