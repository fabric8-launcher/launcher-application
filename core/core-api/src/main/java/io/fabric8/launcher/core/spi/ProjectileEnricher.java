package io.fabric8.launcher.core.spi;

import java.util.function.Consumer;

import io.fabric8.launcher.core.api.Projectile;

/**
 * Enriches a {@link Projectile} before it is launched.
 * They are never called if the project is downloaded as a ZIP
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface ProjectileEnricher extends Consumer<Projectile> {
}
