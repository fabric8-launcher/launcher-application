package io.fabric8.launcher.service.openshift.spi;

import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;

/**
 * Defines the service provider interface for implementations of {@link OpenShiftService}
 * that we won't expose in the API but need for testing or other purposes
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface OpenShiftServiceSpi extends OpenShiftService {

    /**
     * Deletes the specified, required project
     *
     * @param project
     * @return If the operation resulted in a deletion
     * @throws IllegalArgumentException If the project is not specified
     */
    boolean deleteProject(OpenShiftProject project) throws IllegalArgumentException;

    /**
     * Deletes the specified, required project
     *
     * @param projectName
     * @return If the operation resulted in a deletion
     * @throws IllegalArgumentException If the project is not specified
     */
    boolean deleteProject(String projectName) throws IllegalArgumentException;

    /**
     * Delete the specified build config
     *
     * @param namespace the build config namespace
     * @param name the build config name
     */
    void deleteBuildConfig(String namespace, String name);

    /**
     * Delete the specified config map
     *
     * @param namespace the config map namespace
     * @param name the config map name
     */
    void deleteConfigMap(String namespace, String configName);
}
