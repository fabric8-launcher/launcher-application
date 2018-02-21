package io.fabric8.launcher.service.openshift.api;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.client.OpenShiftClient;

/**
 * Defines the operations we support with the OpenShift backend
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface OpenShiftService {

    /**
     * Creates a project with the specified, required name.
     *
     * @param name the name of the project to create
     * @return the created {@link OpenShiftProject}
     * @throws DuplicateProjectException
     * @throws IllegalArgumentException  If the name is not specified
     */
    OpenShiftProject createProject(String name)
            throws DuplicateProjectException, IllegalArgumentException;


    /**
     * Finds an {@link OpenShiftProject} with the specified, required name
     *
     * @param name the name of the project to find
     * @return an {@link Optional} with an existing {@link OpenShiftProject}
     * @throws IllegalArgumentException if the name is not specified
     */
    Optional<OpenShiftProject> findProject(String name) throws IllegalArgumentException;

    /**
     * Returns all the projects in the users namespace.
     *
     * @return the list of projects or empty if there are none
     */
    List<OpenShiftProject> listProjects();

    /**
     * Creates all resources for the given {@link OpenShiftProject}, using the given {@code projectTemplate}.
     * The {@code projectTemplate} is processed on the client side and then applied on OpenShift, where all the
     * described resources are created.
     *
     * @param project             the project in which the pipeline will be created
     * @param sourceRepositoryUri the location of the source repository to build the OpenShift application from
     * @param gitRef              The Git ref to use for the project
     * @param pipelineTemplateUri the location of the pipeline template file
     */
    void configureProject(OpenShiftProject project,
                          URI sourceRepositoryUri,
                          String gitRef,
                          URI pipelineTemplateUri);

    /**
     * Creates all resources for the given {@link OpenShiftProject}, using a standard project template.
     * The project template creates a pipeline build for the passed {@code sourceRepositoryUri}
     *
     * @param project             the project in which the pipeline will be created
     * @param sourceRepositoryUri the location of the source repository to build the OpenShift application from
     */
    void configureProject(OpenShiftProject project, URI sourceRepositoryUri);

    /**
     * Creates all resources for the given {@link OpenShiftProject}, using a standard project template.
     * The project template creates an S2I build for the passed {@code sourceRepositoryUri}
     *
     * @param project                    the project in which the pipeline will be created
     * @param sourceRepositoryUri        the location of the source repository to build the OpenShift application from
     * @param sourceRepositoryContextDir the location within the source repository where the application source can be found
     */
    void configureProject(OpenShiftProject project, InputStream templateStream, URI sourceRepositoryUri, String sourceRepositoryContextDir);

    /**
     * Creates all resources for the given {@link OpenShiftProject}, using the given template and parameters.
     *
     * @param project        the project in which the pipeline will be created
     * @param templateStream the template to read
     * @param parameters     a {@link Map} containing the parameters for the templateStream. Cannot be null
     */
    void configureProject(OpenShiftProject project, InputStream templateStream, Map<String, String> parameters);

    /**
     * @param project The project for which to construct webhook URLs
     * @return the list of webhook URLs associated with the Build Configuration, which
     * GitHub can use to trigger a build upon change pushes (if any).
     * @throws IllegalArgumentException If the project is not specified
     */
    List<URL> getWebhookUrls(final OpenShiftProject project) throws IllegalArgumentException;

    /**
     * Check if the specified project name exists
     *
     * @param name the project name. Required
     * @return <code>true</code> if the project name exists in this Openshift
     * @throws IllegalArgumentException If the project name is not specified
     */
    boolean projectExists(String name) throws IllegalArgumentException;


    /**
     * Returns an optional the service URL for a given project and service name.
     *
     * @param serviceName the service name
     * @param project     The {@link OpenShiftProject} this service belongs to.
     * @return an {@link URL} for the service URL for a given service name.
     * @throws IllegalArgumentException if the URL cannot be found for the serviceName and project
     */
    URL getServiceURL(String serviceName, final OpenShiftProject project) throws IllegalArgumentException;

    /**
     * @return the underlying {@link OpenShiftClient} for advanced operations
     */
    OpenShiftClient getOpenShiftClient();

    // Used in OSIO
    Optional<ConfigMap> getConfigMap(String configName, String namespace);

    ConfigMap createNewConfigMap(String ownerName);

    void createConfigMap(String configName, String namespace, ConfigMap configMap);

    void updateConfigMap(String configName, String namespace, Map<String, String> data);

    void triggerBuild(String projectName, String namespace);

    void applyBuildConfig(BuildConfig buildConfig, String namespace, String sourceName);
}