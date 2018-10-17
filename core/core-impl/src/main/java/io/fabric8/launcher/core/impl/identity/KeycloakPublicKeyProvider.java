package io.fabric8.launcher.core.impl.identity;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
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
import io.fabric8.launcher.base.identity.RSAPublicKeyConverter;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.PublicKeyProvider;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static io.fabric8.launcher.base.http.HttpClient.getContent;
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
        this.identity = Objects.requireNonNull(identity, "Token identity must be specified");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must be specified");
    }

    @Override
    public Optional<RSAPublicKey> getKey(String keyId) {
        final String url = this.keycloakParameters.buildKeysUrl();
        final Request request = securedRequest(identity)
                .url(url)
                .build();
        try {
            final Map<String, RSAPublicKey> publicKeys = httpClient.executeAndMap(request, KeycloakPublicKeyProvider::findKeys);
            final RSAPublicKey publicKey = Objects.requireNonNull(publicKeys).get(keyId);

            return Optional.ofNullable(publicKey);
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Error while fetching keys from keycloak for kid: " + keyId, e);
            return Optional.empty();
        }
    }

    private static Map<String, RSAPublicKey> findKeys(Response r) {
        try (final ResponseBody body = r.body()) {
            final String content = getContent(body);
            final JsonNode node = JsonUtils.readTree(content);
            if (!r.isSuccessful()) {
                throw new IllegalStateException(extractFieldFromNodeOrDefaultTo(node, "errorMessage", ""));
            }
            return findAllPublicKeys(node);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while fetching token from keycloak", e);
        }
    }

    private static Map<String, RSAPublicKey> findAllPublicKeys(JsonNode node) {
        if (!node.hasNonNull("keys")) {
            logger.warning(String.format("Expected 'keys' to be present in the response:\n %s", node.asText()));
            return Collections.emptyMap();
        }
        final Map<String, RSAPublicKey> publicKeys = new HashMap<>();
        node.get("keys")
                .iterator()
                .forEachRemaining(keyNode -> {
                                      final RSAPublicKey publicKey = transformToRsa(keyNode);
                                      publicKeys.put(extractFieldFromNodeOrDefaultTo(keyNode, "kid", "kid"), publicKey);
                                  }
                );
        return publicKeys;
    }

    private static RSAPublicKey transformToRsa(JsonNode keyNode) {
        final String alg = keyNode.get("kty").asText();
        if (!Objects.equals(alg, RSAPublicKeyConverter.PUBLIC_KEY_ALGORITHM)) {
            throw new IllegalStateException("Expecting " + RSAPublicKeyConverter.PUBLIC_KEY_ALGORITHM + " but got " + alg);
        }
        final String modulus = extractFieldFromNodeOrDefaultTo(keyNode, "n", null);
        final String exponent = extractFieldFromNodeOrDefaultTo(keyNode, "e", null);
        return RSAPublicKeyConverter.fromJWK(modulus, exponent);
    }

    private static String extractFieldFromNodeOrDefaultTo(JsonNode node, String name, String defaultValue) {
        return Optional.ofNullable(node.get(name)).orElse(new TextNode(defaultValue)).asText();
    }

}