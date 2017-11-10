package io.fabric8.launcher.service.github.impl.kohsuke;

import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.impl.kohsuke.KohsukeGitHubServiceFactoryImpl;
import io.fabric8.launcher.base.identity.IdentityFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link KohsukeGitHubServiceFactoryImpl}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubServiceProducerTest {

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
