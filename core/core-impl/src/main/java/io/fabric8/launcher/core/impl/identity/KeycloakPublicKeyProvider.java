package io.fabric8.launcher.core.impl.identity;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.PublicKeyProvider;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static io.fabric8.launcher.base.http.Requests.securedRequest;

@RequestScoped
@Application(Application.ApplicationType.LAUNCHER)
public class KeycloakPublicKeyProvider implements PublicKeyProvider {

    private static final Logger logger = Logger.getLogger(KeycloakPublicKeyProvider.class.getName());

    private final KeycloakParameters keycloakParameters;

    private final TokenIdentity identity;

    private final HttpClient httpClient;

    @Inject
    public KeycloakPublicKeyProvider(final KeycloakParameters keycloakParameters, TokenIdentity identity, final HttpClient httpClient) {
        this.keycloakParameters = Objects.requireNonNull(keycloakParameters, "keycloakParameters must be specified");
        this.identity = identity;
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must be specified");
    }

    @Override
    public Optional<String> getKey(String keyId) {
        final String url = this.keycloakParameters.buildKeysUrl();
        final Request request = securedRequest(identity)
                .url(url)
                .build();
        try {
            final Map<String, String> publicKeys = httpClient.executeAndMap(request, KeycloakPublicKeyProvider::findKeys);
            return Optional.ofNullable(Objects.requireNonNull(publicKeys).get(keyId));
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Error while fetching keys from keycloak for kid: " + keyId, e);
            return Optional.empty();
        }
    }

    private static Map<String, String> findKeys(Response r) {
        try (final ResponseBody body = r.body()) {
            if (body == null) {
                return Collections.emptyMap();
            }
            final JsonNode node = JsonUtils.readTree(body.string());
            if (r.isSuccessful()) {
                return findAllPublicKeys(node);
            }
            throw new IllegalStateException(node.get("errorMessage").asText());
        } catch (final IOException e) {
            throw new IllegalStateException("Error while fetching token from keycloak", e);
        }
    }

    private static Map<String, String> findAllPublicKeys(JsonNode node) {
        final Map<String, String> publicKeys = new HashMap<>();
        node.get("keys")
                .iterator()
                .forEachRemaining(keyNode -> publicKeys.put(extractFieldFromNodeOrDefaultTo(keyNode, "kid", "kid"),
                                                            extractFieldFromNodeOrDefaultTo(keyNode, "publicKey", null)));
        return publicKeys;
    }

    private static String extractFieldFromNodeOrDefaultTo(JsonNode node, String name, String defaultValue) {
        return Optional.ofNullable(node.get(name)).orElse(new TextNode(defaultValue)).asText();
    }

}