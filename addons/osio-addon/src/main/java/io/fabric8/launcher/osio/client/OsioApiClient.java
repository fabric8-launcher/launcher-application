package io.fabric8.launcher.osio.client;


import java.net.URI;
import java.util.Optional;

/**
 * Client to request Osio api
 */
public interface OsioApiClient {

    /**
     * Get the token for the specified serviceName
     *
     * @param serviceName the service name
     * @return the token
     */
    Optional<String> getTokenForService(String serviceName);

    /**
     * Get the logged in tenant
     *
     * @return the {@link Tenant}
     */
    Tenant getTenant();

    /**
     * Find the space for the given id
     *
     * @param id the space id
     * @return the {@link Space}
     */
    Space findSpaceById(String id);

    /**
     * Create a code base with the specified repository
     * @param spaceId the spaceId
     * @param stackId the stackId
     * @param repositoryCloneUri the repository clone {@link URI}
     */
    void createCodeBase(String spaceId, String stackId, URI repositoryCloneUri);
}
