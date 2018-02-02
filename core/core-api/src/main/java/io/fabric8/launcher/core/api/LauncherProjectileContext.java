package io.fabric8.launcher.core.api;

/**
 * The context necessary to create a {@link Projectile}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface LauncherProjectileContext extends ProjectileContext {

    String getProjectName();

    String getGitOrganization();

    String getGitRepository();

    // Move the methods below to OSIO
    String getTargetEnvironment();

    String getPipelineId();

    String getSpacePath();

}