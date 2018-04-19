package io.fabric8.launcher.osio.client;


import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import okhttp3.Request;

import static io.fabric8.launcher.base.http.Requests.securedRequest;
import static io.fabric8.launcher.osio.OsioConfigs.getAuthUrl;
import static io.fabric8.utils.URLUtils.pathJoin;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Client to request Osio auth api
 */
@ApplicationScoped
public class OsioAuthClient {

    private static final String GITHUB_SERVICENAME = "https://github.com";

    private final HttpClient httpClient;

    @Inject
    public OsioAuthClient(final HttpClient httpClient) {
        this.httpClient = requireNonNull(httpClient, "httpClient must be specified");
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
        this.httpClient = null;
    }

    /**
     * Get the token for the specified serviceName
     *
     * @param service the service name
     * @return the identity
     */
    public CompletableFuture<Optional<Identity>> getIdentity(final TokenIdentity identity, final String service) {
        if (service.equals(IdentityProvider.ServiceType.OPENSHIFT)) {
            return completedFuture(Optional.of(identity));
        }
        final Request gitHubTokenRequest = newAuthorizedRequestBuilder(identity, "/api/token?for=" + getServiceName(service)).build();
        return httpClient.executeAndParseJsonAsync(gitHubTokenRequest, tree -> TokenIdentity.of(tree.get("access_token").asText()));
    }

    private static Request.Builder newAuthorizedRequestBuilder(final TokenIdentity identity, final String path) {
        return securedRequest(identity)
                .url(pathJoin(getAuthUrl(), path));
    }

    private static String getServiceName(final String service) {
        if (service.equals(IdentityProvider.ServiceType.GITHUB)) {
            return GITHUB_SERVICENAME;
        }
        return service;
    }

}
