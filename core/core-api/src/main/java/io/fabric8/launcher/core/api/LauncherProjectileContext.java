package io.fabric8.launcher.core.api;

/**
 * The context necessary to create a {@link Projectile} through Fabric8 Launcher (developers.redhat.com/launch)
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface LauncherProjectileContext extends CreateProjectileContext {

    String getProjectName();

    String getGitOrganization();

    String getGitRepository();

}