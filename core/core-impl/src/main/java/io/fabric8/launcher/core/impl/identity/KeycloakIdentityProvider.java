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
import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import okhttp3.Request;

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

    public static final String LAUNCHER_MISSIONCONTROL_KEYCLOAK_URL = "LAUNCHER_KEYCLOAK_URL";

    public static final String LAUNCHER_MISSIONCONTROL_KEYCLOAK_REALM = "LAUNCHER_KEYCLOAK_REALM";

    private static final Logger logger = Logger.getLogger(KeycloakIdentityProvider.class.getName());

    private static final String TOKEN_URL_TEMPLATE = "%s/realms/%s/broker/%s/token";

    private final String keyCloakURL;

    private final String realm;

    private HttpClient httpClient;


    @Inject
    public KeycloakIdentityProvider(final HttpClient httpClient) {
        this(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_KEYCLOAK_URL),
             EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_KEYCLOAK_REALM));
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must be specified");
    }

    public KeycloakIdentityProvider(String keyCloakURL, String realm) {
        this.keyCloakURL = keyCloakURL;
        this.realm = realm;
        httpClient = HttpClient.createForTest();
    }

    @Override
    public CompletableFuture<Optional<Identity>> getIdentityAsync(final TokenIdentity authorization, final String provider) {
        requireNonNull(authorization, "authorization must be specified.");
        requireNonNull(provider, "provider must be specified.");

        String url = buildURL(keyCloakURL, realm, provider);
        return getToken(url, authorization).handle((r, e) -> {
            if (e != null) {
                logger.log(Level.FINE, "Error while fetching token from keycloak for provider: " + provider, e);
                return Optional.empty();
            }
            return r;
        });
    }

    static String buildURL(String host, String realm, String provider) {
        return String.format(TOKEN_URL_TEMPLATE, host, realm, provider);
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
        return httpClient.executeAndMapAsync(request, r -> {
            try {
                final String content = r.body().string();
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
        });
    }
}
