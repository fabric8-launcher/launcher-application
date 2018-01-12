package io.fabric8.launcher.service.gitlab.impl;

import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class GitLabServiceTest {

    @Test
    public void gitLabUserIsReturned() {
        TokenIdentity id = IdentityFactory.createFromToken(System.getenv("GITLAB_PRIVATE_TOKEN"));
        GitLabServiceImpl gitLabService = new GitLabServiceImpl(id);
        GitUser user = gitLabService.getLoggedUser();
        Assert.assertNotNull(user);
        Assertions.assertThat(user.getLogin()).isEqualTo(System.getenv("GITLAB_USERNAME"));
    }

    @Test
    public void repositoryDoesNotExist() {
        TokenIdentity id = IdentityFactory.createFromToken(System.getenv("GITLAB_PRIVATE_TOKEN"));
        GitLabServiceImpl gitLabService = new GitLabServiceImpl(id);
        GitRepository repo = gitLabService.getRepository("RepositoryDoesNotExist");
        Assert.assertNull(repo);
    }

    @Test
    public void repositoryExists() {
        TokenIdentity id = IdentityFactory.createFromToken(System.getenv("GITLAB_PRIVATE_TOKEN"));
        GitLabServiceImpl gitLabService = new GitLabServiceImpl(id);
        GitRepository repo = gitLabService.getRepository("Teste");
        Assert.assertNotNull(repo);
    }

}
