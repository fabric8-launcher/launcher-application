package io.fabric8.launcher.service.gitlab.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.fabric8.launcher.service.gitlab.api.GitLabService;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static io.fabric8.launcher.service.gitlab.api.GitLabEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class GitLabServiceIT {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private GitLabService gitLabService = new GitLabServiceFactoryImpl().create();

    private List<GitRepository> repositoriesToDelete = new ArrayList<>();

    @Test
    public void gitLabUserIsReturned() {
        GitUser user = gitLabService.getLoggedUser();
        softly.assertThat(user).isNotNull();
        softly.assertThat(user.getLogin()).isEqualTo(System.getenv(LAUNCHER_MISSIONCONTROL_GITLAB_USERNAME));
    }

    @Test
    public void repositoryDoesNotExist() {
        Optional<GitRepository> repo = gitLabService.getRepository("RepositoryDoesNotExist");
        softly.assertThat(repo).isNotPresent();
    }

    @Test
    public void repositoryExists() {
        createRepository("foo", "Something");
        Optional<GitRepository> repo = gitLabService.getRepository("foo");
        softly.assertThat(repo).isPresent();
    }

    @Test
    public void createRepository() {
        GitRepository repo = createRepository("my-awesome-repository", "Created from integration tests");
        softly.assertThat(repo).isNotNull();
        Optional<GitRepository> repository = gitLabService.getRepository(repo.getFullName());
        softly.assertThat(repository).isPresent();
    }

    @Test
    public void createHook() throws Exception {
        GitRepository repo = createRepository("my-awesome-repository", "Created from integration tests");
        GitHook hook = gitLabService.createHook(repo, new URL("http://my-hook.com"),
                                                GitLabWebHookEvent.PUSH.name(), GitLabWebHookEvent.MERGE_REQUESTS.name());
        softly.assertThat(hook).isNotNull();
        softly.assertThat(hook.getName()).isNotEmpty();
        softly.assertThat(hook.getUrl()).isEqualTo("http://my-hook.com");
        softly.assertThat(hook.getEvents()).containsExactly("push", "merge_requests");
    }


    @Test
    public void deleteHook() throws Exception {
        GitRepository repo = createRepository("my-awesome-repository", "Created from integration tests");
        GitHook hook = gitLabService.createHook(repo, new URL("http://my-hook.com"),
                                                GitLabWebHookEvent.PUSH.name(), GitLabWebHookEvent.MERGE_REQUESTS.name());
        ((GitServiceSpi) gitLabService).deleteWebhook(repo, hook);
        Optional<GitHook> deletedHook = ((GitServiceSpi) gitLabService).getWebhook(repo, new URL(hook.getUrl()));
        softly.assertThat(deletedHook).isNotPresent();
    }

    @After
    public void tearDown() {
        for (GitRepository repo : repositoriesToDelete) {
            ((GitServiceSpi) gitLabService).deleteRepository(repo);
        }
        repositoriesToDelete.clear();
    }

    private GitRepository createRepository(String name, String description) {
        GitRepository repository = gitLabService.createRepository(name, description);
        repositoriesToDelete.add(repository);
        return repository;
    }

}
