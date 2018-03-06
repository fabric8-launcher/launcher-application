package io.fabric8.launcher.service.github;

import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.service.git.api.GitService;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link KohsukeGitHubServiceFactory}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubServiceFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void identityCannotBeNull() {
        new KohsukeGitHubServiceFactory().create(null);
    }

    @Test
    public void createsInstance() {
        // when
        final GitService service = new KohsukeGitHubServiceFactory().create(IdentityFactory.createFromUserPassword("test", "test"));
        // then
        Assert.assertNotNull("instance was not created", service);
    }
}
