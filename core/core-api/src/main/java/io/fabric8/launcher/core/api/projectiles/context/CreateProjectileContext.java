package io.fabric8.launcher.core.api.projectiles.context;

import io.fabric8.launcher.core.api.ProjectileContext;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface CreateProjectileContext extends ProjectileContext, BoosterCapable, CoordinateCapable, ProjectNameCapable, IDEGenerationCapable {

}
