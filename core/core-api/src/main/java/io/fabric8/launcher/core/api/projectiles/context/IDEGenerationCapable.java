package io.fabric8.launcher.core.api.projectiles.context;

import java.util.List;

import io.fabric8.launcher.booster.catalog.rhoar.Runtime;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface IDEGenerationCapable {
    /**
     * @return a {@link List} of the IDEs to generate descriptors
     */
    List<String> getSupportedIDEs();

    Runtime getRuntime();
}
