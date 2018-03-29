package io.fabric8.launcher.osio.client.api;


/**
 * Client to request Osio auth api
 */
public interface OsioJenkinsClient {

    /**
     * Ensure credentials exist for the specified git username
     *
     * @param gitUserName the git username
     */
    void ensureCredentials(String gitUserName);

}
