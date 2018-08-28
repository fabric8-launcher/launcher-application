package io.fabric8.launcher.osio.client;


import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import okhttp3.Request;

import static io.fabric8.launcher.base.http.Requests.securedRequest;
import static io.fabric8.launcher.base.http.Requests.urlEncode;
import static io.fabric8.launcher.osio.OsioConfigs.getAuthUrl;
import static io.fabric8.utils.URLUtils.pathJoin;
import static java.util.Objects.requireNonNull;

/**
 * Client to request Osio auth api
 */
@ApplicationScoped
@Application(Application.ApplicationType.OSIO)
public class OsioIdentityProvider implements IdentityProvider {

    private static final String GITHUB_SERVICE_NAME = "https://github.com";

    private static final Logger logger = Logger.getLogger(OsioIdentityProvider.class.getName());

    private final HttpClient httpClient;

    @Inject
    public OsioIdentityProvider(final HttpClient httpClient) {
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
    protected OsioIdentityProvider() {
        this.httpClient = null;
    }

    @Override
    public Optional<Identity> getIdentity(TokenIdentity identity, String provider) {
        switch (provider) {
            case IdentityProvider.ServiceType.OPENSHIFT:
                return Optional.of(identity);
            case IdentityProvider.ServiceType.GITHUB:
                try {
                    return httpClient.executeAndParseJson(request(identity, GITHUB_SERVICE_NAME), OsioIdentityProvider::parseResult);
                } catch (final Exception e) {
                    logger.log(Level.FINE, "Error while fetching token from osio auth for provider: " + provider, e);
                    return Optional.empty();
                }
            default:
                try {
                    return httpClient.executeAndParseJson(request(identity, provider), OsioIdentityProvider::parseResult);
                } catch (final Exception e) {
                    logger.log(Level.WARNING, "Error while fetching token from osio auth for provider: " + provider, e);
                    return Optional.empty();
                }
        }
    }

    @Override
    public CompletableFuture<Optional<Identity>> getIdentityAsync(final TokenIdentity identity, final String provider) {
        switch (provider) {
            case IdentityProvider.ServiceType.OPENSHIFT:
                return CompletableFuture.completedFuture(Optional.of(identity));
            case IdentityProvider.ServiceType.GITHUB:
                return httpClient.executeAndParseJsonAsync(request(identity, GITHUB_SERVICE_NAME), OsioIdentityProvider::parseResult)
                        .handle((r, e) -> {
                            if (e != null) {
                                logger.log(Level.FINE, "Error while fetching token from osio auth for provider: " + provider, e);
                                return Optional.empty();
                            }
                            return r;
                        });
            default:
                return httpClient.executeAndParseJsonAsync(request(identity, provider), OsioIdentityProvider::parseResult)
                        .handle((r, e) -> {
                            if (e != null) {
                                logger.log(Level.WARNING, "Error while fetching token from osio auth for provider: " + provider, e);
                                return Optional.empty();
                            }
                            return r;
                        });
        }
    }

    private static Request request(final TokenIdentity identity, final String service) {
        return newAuthorizedRequestBuilder(identity, "/api/token?for=" + urlEncode(getServiceName(service))).build();
    }

    private static Identity parseResult(final JsonNode tree) {
        return TokenIdentity.of(tree.get("access_token").asText());
    }

    private static Request.Builder newAuthorizedRequestBuilder(final TokenIdentity identity, final String path) {
        return securedRequest(identity)
                .url(pathJoin(getAuthUrl(), path));
    }

    private static String getServiceName(final String service) {
        if (service.equals(IdentityProvider.ServiceType.GITHUB)) {
            return GITHUB_SERVICE_NAME;
        }
        return service;
    }

}
