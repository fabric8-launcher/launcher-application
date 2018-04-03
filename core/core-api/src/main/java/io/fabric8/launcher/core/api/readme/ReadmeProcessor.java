package io.fabric8.launcher.core.api.readme;

import java.io.IOException;
import java.util.Map;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;

/**
 * Reads the contents from the launcher-documentation repository and process it
 */
public interface ReadmeProcessor {

    /**
     * Get the readme template for the specified mission
     *
     * @param mission the {@link Mission}
     * @return the readme template content
     * @throws IOException
     */
    String getReadmeTemplate(Mission mission) throws IOException;

    /**
     * Get the runtime properties for the specified parameters
     *
     * @param deploymentType the deployment type
     * @param mission the {@lin Mission}
     * @param runtime the {@link Runtime}
     * @return
     * @throws IOException
     */
    Map<String, String> getRuntimeProperties(String deploymentType, Mission mission, Runtime runtime) throws IOException;

    /**
     * Process the template with the specified values
     *
     * @param template the template to render
     * @param values to values to set in the template
     * @return the processed result
     */
    String processTemplate(String template, Map<String, String> values);
}
