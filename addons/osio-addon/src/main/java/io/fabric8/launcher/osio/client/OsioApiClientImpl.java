package io.fabric8.launcher.osio.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.launcher.base.http.ExternalRequest;
import io.fabric8.launcher.base.http.HttpException;
import io.fabric8.launcher.base.identity.IdentityHelper;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.osio.steps.WitSteps;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import static io.fabric8.launcher.base.http.ExternalRequest.readJson;
import static io.fabric8.launcher.osio.OsioConfigs.ExternalServices.getCodebaseCreateUrl;
import static io.fabric8.launcher.osio.OsioConfigs.ExternalServices.getSpaceByIdUrl;
import static io.fabric8.launcher.osio.OsioConfigs.ExternalServices.getTenantIdentityUrl;
import static io.fabric8.launcher.osio.OsioConfigs.ExternalServices.getTenantNamespacesUrl;
import static io.fabric8.launcher.osio.OsioConfigs.ExternalServices.getTokenForServiceUrl;

public final class OsioApiClientImpl implements OsioApiClient {
    private static final Logger LOG = Logger.getLogger(WitSteps.class.getName());

    private final TokenIdentity osioToken;

    public OsioApiClientImpl(final TokenIdentity osioToken) {
        this.osioToken = osioToken;
    }

    @Override
    public Optional<String> getTokenForService(final String serviceName) {
        final String authorizationHeader = IdentityHelper.createRequestAuthorizationHeaderValue(osioToken);
        final Request gitHubTokenRequest = new Request.Builder()
                .url(getTokenForServiceUrl() + serviceName)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .build();
        return ExternalRequest.readJson(gitHubTokenRequest, tree -> tree.get("access_token").asText());
    }

    @Override
    public Tenant getTenant() {
        final String authorizationHeader = IdentityHelper.createRequestAuthorizationHeaderValue(osioToken);
        final Request userInfoRequest = new Request.Builder()
                .url(getTenantIdentityUrl())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .build();

        final Request namespacesRequest = new Request.Builder()
                .url(getTenantNamespacesUrl())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .build();

        final Tenant tenant = readJson(userInfoRequest, tree -> readUserInfo(tree, osioToken))
                .map(builder -> readJson(namespacesRequest, namespaces -> addNamespaces(builder, namespaces)).get())
                .orElseThrow(() -> new BadTenantException("Tenant not found"));
        return tenant;
    }

    @Override
    public Space findSpaceById(String id) {
        final Request request = new Request.Builder()
                .header("Authorization", "Bearer " + osioToken.getToken())
                .url(getSpaceByIdUrl(id))
                .build();
        return ExternalRequest.readJson(request, tree -> {
            JsonNode data = tree.get("data");
            JsonNode attributes = data.get("attributes");
            return ImmutableSpace.builder()
                    .id(data.get("id").textValue())
                    .name(attributes.get("name").textValue())
                    .build();
        }).orElseThrow(() -> new IllegalArgumentException("Space ID not found:" + id));
    }

    @Override
    public void createCodeBase(final String spaceId, final String stackId, final URI repositoryCloneUri) {
        final String payload = "{\n" +
                "  \"data\": {\n" +
                "    \"attributes\": {\n" +
                "      \"stackId\": \"" + stackId + "\",\n" +
                "      \"type\": \"git\",\n" +
                "      \"url\": \"" + repositoryCloneUri + "\"\n" +
                "    },\n" +
                "    \"type\": \"codebases\"\n" +
                "  }\n" +
                "}";

        final Request request = new Request.Builder()
                .url(getCodebaseCreateUrl(spaceId))
                .header("Authorization", "Bearer " + osioToken.getToken())
                .post(RequestBody.create(MediaType.parse("application/json"), payload))
                .build();
        ExternalRequest.execute(request, response -> {
            if (response.code() == 409) {
                // Duplicate. This can be ignored for now as there is no connection in the 'beginning' of the wizard to
                // verify what is in the codebase API
                LOG.log(Level.FINE, () -> "Duplicate codebase for spaceId " + spaceId + " and repository " + repositoryCloneUri);
            } else if (!response.isSuccessful()) {
                assert response.body() != null;
                String message = response.message();
                try {
                    String body = response.body().string();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode errors = mapper.readTree(body).get("errors");
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw, true);
                    for (JsonNode error : errors) {
                        pw.println(error.get("detail").asText());
                    }
                    message = sw.toString();
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Error while reading error from WIT", e);
                }
                throw new HttpException(response.code(), message);
            }
            return null;
        });
    }

    private static ImmutableTenant.Builder readUserInfo(JsonNode tree, TokenIdentity token) {
        final JsonNode attributes = tree.get("data").get("attributes");
        return ImmutableTenant.builder()
                .identity(token)
                .email(attributes.get("email").asText())
                .username(attributes.get("username").asText());
    }

    private static Tenant addNamespaces(ImmutableTenant.Builder builder, JsonNode tree) {
        final JsonNode namespaces = tree.get("data").get("attributes").get("namespaces");
        for (JsonNode namespaceJson : namespaces) {
            Tenant.Namespace namespace = ImmutableNamespace.builder()
                    .name(namespaceJson.get("name").asText())
                    .type(namespaceJson.get("type").asText())
                    .clusterUrl(namespaceJson.get("cluster-url").asText())
                    .clusterConsoleUrl(namespaceJson.get("cluster-console-url").asText())
                    .build();
            builder.addNamespace(namespace);
        }
        return builder.build();
    }
}
