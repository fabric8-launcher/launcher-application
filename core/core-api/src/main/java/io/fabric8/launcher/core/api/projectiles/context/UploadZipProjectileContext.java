package io.fabric8.launcher.core.api.projectiles.context;

import java.io.InputStream;

import io.fabric8.launcher.core.api.ProjectileContext;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
public interface UploadZipProjectileContext extends ProjectileContext, ProjectNameCapable, GitCapable {
    InputStream getZipContents();
}
