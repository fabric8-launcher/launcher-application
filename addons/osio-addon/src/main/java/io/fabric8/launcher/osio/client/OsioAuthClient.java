package io.fabric8.launcher.osio.client;


import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.http.Requests;
import io.fabric8.launcher.base.identity.TokenIdentity;
import okhttp3.Request;

import static io.fabric8.launcher.base.http.Requests.securedRequest;
import static io.fabric8.launcher.osio.OsioConfigs.getAuthUrl;
import static io.fabric8.utils.URLUtils.pathJoin;
import static java.util.Objects.requireNonNull;

/**
 * Client to request Osio auth api
 */
@RequestScoped
public class OsioAuthClient {

    private final TokenIdentity authorization;

    private final Requests requests;

    @Inject
    public OsioAuthClient(final TokenIdentity authorization, Requests requests) {
        this.authorization = requireNonNull(authorization, "authorization must be specified.");
        this.requests = requireNonNull(requests, "requests must be specified");
    }

    /**
     * no-args constructor used by CDI for proxying only
     * but is subsequently replaced with an instance
     * created using the above constructor.
     *
     * @deprecated do not use this constructor
     */
    @Deprecated
    protected OsioAuthClient() {
        this.authorization = null;
        this.requests = null;
    }

    /**
     * Get the token for the specified serviceName
     *
     * @param serviceName the service name
     * @return the token
     */
    public Optional<String> getTokenForService(final String serviceName) {
        final Request gitHubTokenRequest = newAuthorizedRequestBuilder("/api/token?for=" + serviceName).build();
        return requests.executeAndParseJson(gitHubTokenRequest, tree -> tree.get("access_token").asText());
    }

    private Request.Builder newAuthorizedRequestBuilder(final String path) {
        return securedRequest(authorization)
                .url(pathJoin(getAuthUrl(), path));
    }

}
