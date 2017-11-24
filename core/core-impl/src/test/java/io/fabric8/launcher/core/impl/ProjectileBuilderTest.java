package io.fabric8.launcher.core.impl;

import java.nio.file.Paths;

import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.CreateProjectileBuilder;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.ProjectileBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases to ensure the {@link ProjectileBuilder}
 * is working as contracted
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class ProjectileBuilderTest {

    private static final String SOME_VALUE = "test";

    @Test
    public void createsCreateProjectile() {
        final Projectile projectile = this.getPopulatedCreateProjectileBuilder().build();
        Assert.assertNotNull("projectile should have been created", projectile);
    }

    @Test
    public void githubRepositoryDescriptionShouldHaveBeenSet() {
        final CreateProjectile projectile = this.getPopulatedCreateProjectileBuilder().gitHubRepositoryDescription("DESCRIPTION").build();
        Assert.assertEquals("GitHub Repository Description should have been set", "DESCRIPTION", projectile.getGitHubRepositoryDescription());
    }


    @Test(expected = IllegalStateException.class)
    public void requiresProjectLocation() {
        ProjectileBuilder.newInstance().createType().projectLocation(null).build();
    }

    /**
     * @return A builder with all properties set so we can manually
     * set one property to empty and test {@link ForkProjectileBuilder#build()}
     */
    private CreateProjectileBuilder getPopulatedCreateProjectileBuilder() {
        return ProjectileBuilder.newInstance()
                .openShiftProjectName(SOME_VALUE)
                .gitHubIdentity(IdentityFactory.createFromToken(SOME_VALUE))
                .openShiftIdentity(IdentityFactory.createFromToken(SOME_VALUE))
                .createType()
                .projectLocation(Paths.get("."));
    }
}
