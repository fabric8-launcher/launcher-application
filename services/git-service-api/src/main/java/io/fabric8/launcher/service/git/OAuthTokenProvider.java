package io.fabric8.launcher.service.git;

import io.fabric8.launcher.service.git.api.GitServiceConfig;

/**
 * Fetch oauth tokens for git providers
 */
public interface OAuthTokenProvider {
    /**
     * Get the token for the specified temporary code.
     * @param code the code to fetch the auth token with
     * @param config the gitConfig that holds the clientId secret and url
     * @return token that can be send back to the server in order to be stateless
     */
    String getToken(String code, GitServiceConfig config);
}
