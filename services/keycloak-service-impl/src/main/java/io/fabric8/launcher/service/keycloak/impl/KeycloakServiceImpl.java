package io.fabric8.launcher.service.keycloak.impl;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.keycloak.api.KeycloakService;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static io.fabric8.launcher.base.http.HttpClient.securedRequest;
import static java.util.Objects.requireNonNull;

/**
 * The implementation of the {@link KeycloakService}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class KeycloakServiceImpl implements KeycloakService {

    public static final String LAUNCHER_MISSIONCONTROL_KEYCLOAK_URL = "LAUNCHER_KEYCLOAK_URL";

    public static final String LAUNCHER_MISSIONCONTROL_KEYCLOAK_REALM = "LAUNCHER_KEYCLOAK_REALM";

    @Inject
    public KeycloakServiceImpl() {
        this(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_KEYCLOAK_URL),
             EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_KEYCLOAK_REALM));
    }

    public KeycloakServiceImpl(String keyCloakURL, String realm) {
        this.keyCloakURL = keyCloakURL;
        this.realm = realm;
        this.gitHubURL = buildURL(keyCloakURL, realm, "github");
        this.openShiftURL = buildURL(keyCloakURL, realm, "openshift-v3");

        httpClient = new OkHttpClient.Builder().build();
    }

    private static final Logger logger = Logger.getLogger(KeycloakServiceImpl.class.getName());

    private static final String TOKEN_URL_TEMPLATE = "%s/realms/%s/broker/%s/token";

    private final String keyCloakURL;

    private final String realm;

    private final String gitHubURL;

    private final String openShiftURL;

    private final OkHttpClient httpClient;

    /**
     * GET https://sso.openshift.io/auth/realms/rh-developers-launch/broker/openshift-v3/token
     * Authorization: Bearer <authorizationHeader>
     *
     * @param authorization the keycloak access authorization
     * @return
     */
    @Override
    public Identity getOpenShiftIdentity(final TokenIdentity authorization) {
        requireNonNull(authorization, "authorization must be specified.");

        return TokenIdentity.of(getToken(openShiftURL, authorization));
    }

    /**
     * GET https://sso.openshift.io/auth/realms/rh-developers-launch/broker/github/token
     * Authorization: Bearer <authorizationHeader>
     *
     * @param authorization the keycloak access authorization
     * @return
     */
    @Override
    public Identity getGitHubIdentity(final TokenIdentity authorization) throws IllegalArgumentException {
        requireNonNull(authorization, "authorization must be specified.");

        return TokenIdentity.of(getToken(gitHubURL, authorization));
    }


    @Override
    public Optional<Identity> getIdentity(final TokenIdentity authorization, final String provider) {
        requireNonNull(authorization, "authorization must be specified.");
        requireNonNull(provider, "provider must be specified.");

        String url = buildURL(keyCloakURL, realm, provider);
        Identity identity = null;
        try {
            String providerToken = getToken(url, authorization);
            identity = TokenIdentity.of(providerToken);
        } catch (Exception e) {
            logger.log(Level.FINE, "Error while grabbing token from provider " + provider, e);
        }
        return Optional.ofNullable(identity);

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
    private String getToken(final String url, final TokenIdentity authorization) {
        Request request = securedRequest(authorization)
                .url(url)
                .build();
        Call call = httpClient.newCall(request);
        try (Response response = call.execute()) {
            String content = response.body().string();
            // Keycloak does not respect the content-type
            if (content.startsWith("{")) {
                JsonNode node = JsonUtils.readTree(content);
                if (response.isSuccessful()) {
                    return node.get("access_token").asText();
                } else if (response.code() == 400) {
                    throw new IllegalArgumentException(node.get("errorMessage").asText());
                } else {
                    throw new IllegalStateException(node.get("errorMessage").asText());
                }
            } else {
                //access_token=1bbf10a0009d865fcb2f60d40a0ca706c7ca1e48&scope=admin%3Arepo_hook%2Cgist%2Cread%3Aorg%2Crepo%2Cuser&token_type=bearer
                String tokenParam = "access_token=";
                int idxAccessToken = content.indexOf(tokenParam);
                if (idxAccessToken < 0) {
                    throw new IllegalStateException("Access Token not found");
                }
                return content.substring(idxAccessToken + tokenParam.length(), content.indexOf('&', idxAccessToken + tokenParam.length()));
            }
        } catch (IOException io) {
            throw new IllegalStateException("Error while fetching token from keycloak", io);
        }
    }
}
