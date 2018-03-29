package io.fabric8.launcher.osio.client.api;


import java.net.URI;

/**
 * Client to request Osio wit api
 */
public interface OsioWitClient {

    /**
     * Get the logged user
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
     * Find the space for the given name
     *
     * @param tenantName the tenant name
     * @param spaceName the space name
     * @return the {@link Space}
     */
    Space findSpaceByName(String tenantName, String spaceName);

    /**
     * Create a code base with the specified repository
     *
     * @param spaceId the spaceId
     * @param stackId the stackId
     * @param repositoryCloneUri the repository clone {@link URI}
     */
    void createCodeBase(String spaceId, String stackId, URI repositoryCloneUri);
}
