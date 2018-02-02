package io.fabric8.launcher.core.api;

/**
 * The context necessary to create a {@link CreateProjectile}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface CreateProjectileContext extends ProjectileContext {

    String getGitRepository();

    String getTargetEnvironment();

    String getPipelineId();

    String getSpacePath();

    String getGitOrganization();
}