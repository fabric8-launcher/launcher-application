package io.fabric8.launcher.service.github.impl;

import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.service.github.api.GitHubService;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link KohsukeGitHubServiceFactoryImpl}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubServiceFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void identityCannotBeNull() {
        new KohsukeGitHubServiceFactoryImpl().create(null);
    }

    @Test
    public void createsInstance() {
        // when
        final GitHubService service = new KohsukeGitHubServiceFactoryImpl().create(IdentityFactory.createFromUserPassword("test", "test"));
        // then
        Assert.assertNotNull("instance was not created", service);
    }
}
