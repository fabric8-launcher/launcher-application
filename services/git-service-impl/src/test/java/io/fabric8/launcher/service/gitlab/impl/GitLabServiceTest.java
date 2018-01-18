package io.fabric8.launcher.service.gitlab.impl;

import java.util.Optional;

import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.gitlab.api.GitLabService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class GitLabServiceTest {

    private GitLabService gitLabService;

    @Before
    public void setUp() {
        TokenIdentity id = IdentityFactory.createFromToken(System.getenv("GITLAB_PRIVATE_TOKEN"));
        gitLabService = new GitLabServiceImpl(id);
    }

    @Test
    public void gitLabUserIsReturned() {
        GitUser user = gitLabService.getLoggedUser();
        Assert.assertNotNull(user);
        assertThat(user.getLogin()).isEqualTo(System.getenv("GITLAB_USERNAME"));
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
        GitRepository repo = gitLabService.createRepository("my-awesome-repository", "Created from integration tests");
        assertThat(repo).isNotNull();
        assertThat(gitLabService.getRepository(repo.getFullName())).isPresent();

    }

}
