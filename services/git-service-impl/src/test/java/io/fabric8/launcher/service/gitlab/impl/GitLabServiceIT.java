package io.fabric8.launcher.service.gitlab.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.spi.GitServiceSpi;
import io.fabric8.launcher.service.gitlab.api.GitLabService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import static io.fabric8.launcher.service.gitlab.api.GitLabEnvVarSysPropNames.GITLAB_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class GitLabServiceIT {

    private GitLabService gitLabService = new GitLabServiceFactoryImpl().create();

    private List<GitRepository> repositoriesToDelete = new ArrayList<>();

    @Test
    public void gitLabUserIsReturned() {
        GitUser user = gitLabService.getLoggedUser();
        Assert.assertNotNull(user);
        assertThat(user.getLogin()).isEqualTo(System.getenv(GITLAB_USERNAME));
    }

    @Test
    public void repositoryDoesNotExist() {
        Optional<GitRepository> repo = gitLabService.getRepository("RepositoryDoesNotExist");
        assertThat(repo).isNotPresent();
    }

    @Test
    public void repositoryExists() {
        Optional<GitRepository> repo = gitLabService.getRepository("Teste");
        assertThat(repo).isPresent();
    }

    @Test
    public void createRepository() {
        GitRepository repo = createRepository("my-awesome-repository", "Created from integration tests");
        assertThat(repo).isNotNull();
        Optional<GitRepository> repository = gitLabService.getRepository(repo.getFullName());
        assertThat(repository).isPresent();
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
