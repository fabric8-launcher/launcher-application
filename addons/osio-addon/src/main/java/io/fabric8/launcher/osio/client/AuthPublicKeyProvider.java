package io.fabric8.launcher.osio.client;

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

import static io.fabric8.kubernetes.client.utils.URLUtils.pathJoin;
import static io.fabric8.launcher.base.http.Requests.securedRequest;
import static io.fabric8.launcher.osio.OsioConfigs.getAuthUrl;
import static java.util.Objects.requireNonNull;

@RequestScoped
@Application(Application.ApplicationType.OSIO)
public class AuthPublicKeyProvider implements PublicKeyProvider {

    private static final Logger logger = Logger.getLogger(AuthPublicKeyProvider.class.getName());

    private final TokenIdentity identity;

    private final HttpClient httpClient;

    @Inject
    public AuthPublicKeyProvider(TokenIdentity identity, HttpClient httpClient) {
        this.identity = requireNonNull(identity, "authorization must be specified.");
        this.httpClient = requireNonNull(httpClient, "httpClient must be specified");
    }

    @Override
    public Optional<String> getKey(String keyId) {
        final Request request = securedRequest(identity)
                .url(pathJoin(getAuthUrl(), "/api/token/keys?format=pem")).build();
        try {
            final Map<String, String> publicKeys = httpClient.executeAndMap(request, AuthPublicKeyProvider::findKeys);
            return Optional.ofNullable(Objects.requireNonNull(publicKeys).get(keyId));
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Error while fetching keys from OSIO auth for kid: " + keyId, e);
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
            throw new IllegalStateException(node.get("errors").asText());
        } catch (final IOException e) {
            throw new IllegalStateException("Error while fetching token from OSIO auth service", e);
        }
    }

    private static Map<String, String> findAllPublicKeys(JsonNode node) {
        final Map<String, String> publicKeys = new HashMap<>();
        node.get("keys")
                .iterator()
                .forEachRemaining(keyNode -> publicKeys.put(extractFieldFromNodeOrDefaultTo(keyNode, "kid", "kid"),
                                                            extractFieldFromNodeOrDefaultTo(keyNode, "key", null)));
        return publicKeys;
    }

    private static String extractFieldFromNodeOrDefaultTo(JsonNode node, String name, String defaultValue) {
        return Optional.ofNullable(node.get(name)).orElse(new TextNode(defaultValue)).asText();
    }

}
