package io.openshift.appdev.missioncontrol.core.impl;

import java.nio.file.Paths;

import io.openshift.appdev.missioncontrol.base.identity.IdentityFactory;
import io.openshift.appdev.missioncontrol.core.api.Projectile;
import org.junit.Assert;
import org.junit.Test;
import io.openshift.appdev.missioncontrol.core.api.CreateProjectile;
import io.openshift.appdev.missioncontrol.core.api.CreateProjectileBuilder;
import io.openshift.appdev.missioncontrol.core.api.ForkProjectile;
import io.openshift.appdev.missioncontrol.core.api.ForkProjectileBuilder;
import io.openshift.appdev.missioncontrol.core.api.ProjectileBuilder;

/**
 * Test cases to ensure the {@link ProjectileBuilder}
 * is working as contracted
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class ProjectileBuilderTest {

    private static final String SOME_VALUE = "test";

    private static final String REPO_VALUE = "ALRubinger/testrepo";

    private static final String EMPTY = "";

    @Test(expected = IllegalStateException.class)
    public void requiresSourceGitHubRepo() {
        this.getPopulatedForkProjectileBuilder().sourceGitHubRepo(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void requiresSourceGitHubRepoNotEmpty() {
        this.getPopulatedForkProjectileBuilder().sourceGitHubRepo(EMPTY).build();
    }

    @Test(expected = IllegalStateException.class)
    public void requiresGitHubAccessToken() {
        this.getPopulatedForkProjectileBuilder().gitHubIdentity(null).forkType().build();
    }

    @Test(expected = IllegalStateException.class)
    public void requiresGitRef() {
        this.getPopulatedForkProjectileBuilder().gitRef(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void requiresGitRefNotEmpty() {
        this.getPopulatedForkProjectileBuilder().gitRef(EMPTY).build();
    }

    @Test(expected = IllegalStateException.class)
    public void requiresPipelineTemplatePath() {
        this.getPopulatedForkProjectileBuilder().pipelineTemplatePath(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void requiresPipelineTemplatePathNotEmpty() {
        this.getPopulatedForkProjectileBuilder().pipelineTemplatePath(EMPTY).build();
    }

    @Test
    public void createsForkProjectile() {
        final ForkProjectile projectile = this.getPopulatedForkProjectileBuilder().build();
        Assert.assertNotNull("projectile should have been created", projectile);
    }

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


    @Test
    public void createsForkProjectileWithDefaultedOpenShiftProjectName() {
        final Projectile projectile = ((ForkProjectileBuilder) this.getPopulatedForkProjectileBuilder().openShiftProjectName(null)).build();
        Assert.assertEquals("openshiftProjectName was not defaulted correctly", "testrepo", projectile.getOpenShiftProjectName());
    }

    @Test
    public void createsForkProjectileWithExplicitOpenShiftProjectName() {
        final Projectile projectile = ((ForkProjectileBuilder) this.getPopulatedForkProjectileBuilder().openShiftProjectName("changedfromtest")).build();
        Assert.assertEquals("openshiftProjectName was not set correctly", "changedfromtest", projectile.getOpenShiftProjectName());
    }

    @Test(expected = IllegalStateException.class)
    public void sourceRepoMustBeInCorrectForm() {
        ProjectileBuilder.newInstance().forkType().sourceGitHubRepo("doesntFollowForm").build();
    }

    @Test
    public void sourceRepoMustAcceptDashes() {
        this.mustAcceptDashes("ALRubinger/my-test-thing");
    }

    @Test
    public void sourceOwnerMustAcceptDashes() {
        this.mustAcceptDashes("redhat-organization/something-with-dashes");
    }

    @Test(expected = IllegalStateException.class)
    public void requiresProjectLocation() {
        ProjectileBuilder.newInstance().createType().projectLocation(null).build();
    }

    private void mustAcceptDashes(final String fullRepoName) {
        final Projectile projectile = this.getPopulatedForkProjectileBuilder()
                .sourceGitHubRepo(fullRepoName)
                .build();
        Assert.assertNotNull("projectile should have been created", projectile);
    }

    /**
     * @return A builder with all properties set so we can manually
     * set one property to empty and test {@link ForkProjectileBuilder#build()}
     */
    private ForkProjectileBuilder getPopulatedForkProjectileBuilder() {
        return ProjectileBuilder.newInstance()
                .openShiftProjectName(SOME_VALUE)
                .gitHubIdentity(IdentityFactory.createFromToken(SOME_VALUE))
                .openShiftIdentity(IdentityFactory.createFromToken(SOME_VALUE))
                .forkType()
                .sourceGitHubRepo(REPO_VALUE)
                .gitRef(SOME_VALUE)
                .pipelineTemplatePath(SOME_VALUE);
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
