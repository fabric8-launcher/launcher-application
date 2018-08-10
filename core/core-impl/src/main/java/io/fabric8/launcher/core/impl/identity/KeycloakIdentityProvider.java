package io.fabric8.launcher.core.impl.identity;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static io.fabric8.launcher.base.http.Requests.securedRequest;
import static java.util.Objects.requireNonNull;

/**
 * The implementation of the {@link IdentityProvider}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
@Application(Application.ApplicationType.LAUNCHER)
public class KeycloakIdentityProvider implements IdentityProvider {

    private static final Logger logger = Logger.getLogger(KeycloakIdentityProvider.class.getName());

    private final KeycloakParameters keycloakParameters;

    private final HttpClient httpClient;

    @Inject
    public KeycloakIdentityProvider(final KeycloakParameters keycloakParameters, final HttpClient httpClient) {
        this.keycloakParameters = Objects.requireNonNull(keycloakParameters, "keycloakParameters must be specified");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must be specified");
    }

    @Override
    public CompletableFuture<Optional<Identity>> getIdentityAsync(final TokenIdentity authorization, final String service) {
        requireNonNull(authorization, "authorization must be specified.");
        requireNonNull(service, "service must be specified.");

        String url = this.keycloakParameters.buildUrl(service);
        return getToken(url, authorization).handle((r, e) -> {
            if (e != null) {
                logger.log(Level.FINE, "Error while fetching token from keycloak for provider: " + service, e);
                return Optional.empty();
            }
            return r;
        });
    }

    @Override
    public Optional<Identity> getIdentity(TokenIdentity authorization, String service) {
        requireNonNull(authorization, "authorization must be specified.");
        requireNonNull(service, "service must be specified.");
        String url = this.keycloakParameters.buildUrl(service);
        Request request = securedRequest(authorization)
                .url(url)
                .build();
        try {
            return httpClient.executeAndMap(request, KeycloakIdentityProvider::parseIdentity);
        } catch (final Exception e) {
            logger.log(Level.FINE, "Error while fetching token from keycloak for provider: " + service, e);
            return Optional.empty();
        }
    }

    /**
     * GET https://sso.openshift.io/auth/realms/rh-developers-launch/broker/{brokerType}/token
     * Authorization: Bearer <keycloakAccessToken>
     *
     * @param url
     * @param authorization
     * @return
     */
    private CompletableFuture<Optional<Identity>> getToken(final String url, final TokenIdentity authorization) {
        Request request = securedRequest(authorization)
                .url(url)
                .build();
        return httpClient.executeAndMapAsync(request, KeycloakIdentityProvider::parseIdentity);
    }

    private static Optional<Identity> parseIdentity(Response r) {
        try {
            ResponseBody body = r.body();
            if (body == null) {
                return Optional.empty();
            }
            final String content = body.string();
            // Keycloak does not respect the content-type
            if (content.startsWith("{")) {
                final JsonNode node = JsonUtils.readTree(content);
                if (r.isSuccessful()) {
                    return Optional.of(TokenIdentity.of(node.get("access_token").asText()));
                } else if (r.code() == 400) {
                    throw new IllegalArgumentException(node.get("errorMessage").asText());
                } else {
                    throw new IllegalStateException(node.get("errorMessage").asText());
                }
            } else {
                String tokenParam = "access_token=";
                int idxAccessToken = content.indexOf(tokenParam);
                if (idxAccessToken < 0) {
                    throw new IllegalStateException("Access Token not found");
                }
                final String token = content.substring(idxAccessToken + tokenParam.length(), content.indexOf('&', idxAccessToken + tokenParam.length()));
                return Optional.of(TokenIdentity.of(token));
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Error while fetching token from keycloak", e);
        }
    }
}
