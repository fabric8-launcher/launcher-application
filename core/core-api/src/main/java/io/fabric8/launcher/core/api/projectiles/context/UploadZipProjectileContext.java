package io.fabric8.launcher.core.api.projectiles.context;

import java.io.InputStream;

import io.fabric8.launcher.core.api.ProjectileContext;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface UploadZipProjectileContext extends ProjectileContext, ProjectNameCapable, GitCapable {
    InputStream getZipContents();
}
