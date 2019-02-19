package io.fabric8.launcher.service.git.github;

import io.fabric8.launcher.base.identity.ImmutableUserPasswordIdentity;
import io.fabric8.launcher.service.git.api.GitService;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link GitHubServiceFactory}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubServiceFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void identityCannotBeNull() {
        new GitHubServiceFactory().create(null, null);
    }

    @Test
    public void createsInstance() {
        // when
        final GitService service = new GitHubServiceFactory().create(ImmutableUserPasswordIdentity.of("test", "test"), "test");
        // then
        Assert.assertNotNull("instance was not created", service);
    }
}
