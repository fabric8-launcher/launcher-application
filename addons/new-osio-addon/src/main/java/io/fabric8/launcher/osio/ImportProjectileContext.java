package io.fabric8.launcher.osio;

import io.fabric8.launcher.core.api.ProjectileContext;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface ImportProjectileContext extends ProjectileContext {
    String getGitOrganization();

    String getGitRepository();
}
