package io.fabric8.launcher.osio.client.api;


import java.util.Optional;

/**
 * Client to request Osio auth api
 */
public interface OsioAuthClient {

    /**
     * Get the token for the specified serviceName
     *
     * @param serviceName the service name
     * @return the token
     */
    Optional<String> getTokenForService(String serviceName);

}
