package io.fabric8.launcher.service.gitlab;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.fabric8.launcher.service.gitlab.api.GitLabWebhookEvent;
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
import static io.fabric8.launcher.service.gitlab.api.GitLabEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN;
import static io.fabric8.launcher.service.gitlab.api.GitLabEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class GitLabGitServiceIT {

    private static final String MY_GITLAB_REPO_DESCRIPTION = "Test project created by Integration Tests.";

    @ClassRule
    public static RuleChain ruleChain = RuleChain
        // After recording on a real environment against a real service,
        // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
        .outerRule(createDefaultHoverflyEnvironment()
                .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME, "gitlabUser")
                .andForSimulationOnly(LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN, "aefeajfnUZ3332"))
       .around(createHoverflyProxy("gl-simulation.json", "gitlab.com"));

    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    @Rule
    public final TestName testName = new TestName();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private GitLabGitService gitLabGitService = new GitLabServiceFactory().create();

    private List<GitRepository> repositoriesToDelete = new ArrayList<>();

    @Test
    public void gitLabUserIsReturned() {
        GitUser user = gitLabGitService.getLoggedUser();
        softly.assertThat(user).isNotNull();
        // Relaxed condition as we use different accounts / organizations for actual GL calls - therefore simulation file might contain different username
        softly.assertThat(user.getLogin()).isNotEmpty();
        softly.assertThat(user.getAvatarUrl()).isNotNull();
    }

    @Test
    public void repositoryDoesNotExist() {
        Optional<GitRepository> repo = gitLabGitService.getRepository("RepositoryDoesNotExist");
        softly.assertThat(repo).isNotPresent();
    }

    @Test
    public void repositoryExists() {
        final GitRepository repo = createRepository();
        Optional<GitRepository> repository = gitLabGitService.getRepository(repo.getFullName());
        softly.assertThat(repository).isPresent();
    }

    @Test
    public void shouldCreateRepository() {
        GitRepository repo = createRepository();
        softly.assertThat(repo).isNotNull();
        Optional<GitRepository> repository = gitLabGitService.getRepository(repo.getFullName());
        softly.assertThat(repository).isPresent();
    }

    @Test
    public void createHook() throws Exception {
        GitRepository repo = createRepository();
        GitHook hook = gitLabGitService.createHook(repo, "my secret", new URL("http://my-hook.com"),
                                                   GitLabWebhookEvent.PUSH.name(), GitLabWebhookEvent.MERGE_REQUESTS.name());
        softly.assertThat(hook).isNotNull();
        softly.assertThat(hook.getName()).isNotEmpty();
        softly.assertThat(hook.getUrl()).isEqualTo("http://my-hook.com");
        softly.assertThat(hook.getEvents()).containsExactly("push", "merge_requests");
    }

    @Test
    public void deleteHook() throws Exception {
        GitRepository repo = createRepository();
        GitHook hook = gitLabGitService.createHook(repo, null, new URL("http://my-hook.com"),
                                                   GitLabWebhookEvent.PUSH.name(), GitLabWebhookEvent.MERGE_REQUESTS.name());
        gitLabGitService.deleteWebhook(repo, hook);
        Optional<GitHook> deletedHook = gitLabGitService.getHook(repo, new URL(hook.getUrl()));
        softly.assertThat(deletedHook).isNotPresent();
    }

    @Test
    public void readOrganizations() {
        List<GitOrganization> organizations = gitLabGitService.getOrganizations();
        softly.assertThat(organizations).isNotNull();
    }

    @Test
    public void readRepositories() {
        List<GitRepository> repos = gitLabGitService.getRepositories(null);
        softly.assertThat(repos).isNotNull();
    }

    @After
    public void tearDown() {
        for (GitRepository repo : repositoriesToDelete) {
            ((GitServiceSpi) gitLabGitService).deleteRepository(repo);
        }
        repositoriesToDelete.clear();
    }

    // - Generating repo per test method
    private GitRepository createRepository() {
        GitRepository repository = gitLabGitService.createRepository(generateRepositoryName(), MY_GITLAB_REPO_DESCRIPTION);
        repositoriesToDelete.add(repository);
        return repository;
    }

    private String generateRepositoryName() {
        return this.getClass().getSimpleName() + "-" + testName.getMethodName();
    }
}
