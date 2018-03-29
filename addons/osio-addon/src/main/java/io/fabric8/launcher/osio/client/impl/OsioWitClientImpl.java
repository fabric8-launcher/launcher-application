package io.fabric8.launcher.osio.client.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.core.HttpHeaders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.launcher.base.http.ExternalRequest;
import io.fabric8.launcher.base.http.HttpException;
import io.fabric8.launcher.osio.client.api.BadTenantException;
import io.fabric8.launcher.osio.client.api.ImmutableNamespace;
import io.fabric8.launcher.osio.client.api.ImmutableSpace;
import io.fabric8.launcher.osio.client.api.ImmutableTenant;
import io.fabric8.launcher.osio.client.api.ImmutableUserInfo;
import io.fabric8.launcher.osio.client.api.OsioWitClient;
import io.fabric8.launcher.osio.client.api.Space;
import io.fabric8.launcher.osio.client.api.Tenant;
import io.fabric8.launcher.osio.steps.WitSteps;
import okhttp3.Request;
import okhttp3.Response;

import static io.fabric8.launcher.base.http.ExternalRequest.executeAndParseJson;
import static io.fabric8.launcher.base.identity.IdentityFactory.createFromToken;
import static io.fabric8.launcher.base.identity.IdentityHelper.removeBearerPrefix;
import static io.fabric8.launcher.osio.OsioConfigs.getWitUrl;
import static io.fabric8.utils.URLUtils.pathJoin;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;

public final class OsioWitClientImpl implements OsioWitClient {
    private static final Logger LOG = Logger.getLogger(WitSteps.class.getName());

    private final String authorizationHeader;

    public OsioWitClientImpl(final String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    @Override
    public Tenant getTenant() {
        return ImmutableTenant.builder()
                .userInfo(getUserInfo())
                .namespaces(getNamespaces())
                .identity(createFromToken(removeBearerPrefix(authorizationHeader)))
                .build();
    }

    @Override
    public Space findSpaceById(final String spaceId) {
        final Request request = newAuthorizedRequestBuilder("/api/spaces/" + spaceId).build();
        return executeAndParseJson(request, OsioWitClientImpl::readSpace)
                .orElseThrow(() -> new IllegalArgumentException("Space ID not found:" + spaceId));
    }

    @Override
    public Space findSpaceByName(final String tenantName, final String spaceName) {
        final Request request = newAuthorizedRequestBuilder("/api/namedspaces/" + tenantName + "/" + spaceName).build();
        return executeAndParseJson(request, OsioWitClientImpl::readSpace)
                .orElseThrow(() -> new IllegalArgumentException("Space not found for tenant:" + tenantName + " with name " + spaceName));
    }

    @Override
    public void createCodeBase(final String spaceId, final String stackId, final URI repositoryCloneUri) {
        final String payload = String.format(
                "{\"data\":{\"attributes\":{\n\"stackId\":\"%s\",\"type\":\"git\",\"url\":\"%s\"},\"type\":\"codebases\"}}",
                stackId,
                repositoryCloneUri
        );
        final Request request = newAuthorizedRequestBuilder("/api/spaces/" + spaceId + "/codebases")
                .post(create(parse("application/json"), payload))
                .build();
        ExternalRequest.executeAndConsume(request, r -> validateCodeBaseResponse(spaceId, repositoryCloneUri, r));
    }

    private Tenant.UserInfo getUserInfo() {
        final Request userInfoRequest = newAuthorizedRequestBuilder("/api/user").build();
        return executeAndParseJson(userInfoRequest, OsioWitClientImpl::readUserInfo)
                .orElseThrow(() -> new BadTenantException("UserInfo not found"));
    }

    private List<Tenant.Namespace> getNamespaces() {
        final Request namespacesRequest = newAuthorizedRequestBuilder("/api/user/services").build();
        return executeAndParseJson(namespacesRequest, OsioWitClientImpl::readNamespaces)
                .orElseThrow(() -> new BadTenantException("Namespaces not found"));
    }

    private Request.Builder newAuthorizedRequestBuilder(final String path) {
        return new Request.Builder()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .url(pathJoin(getWitUrl(), path));
    }

    private static Tenant.UserInfo readUserInfo(JsonNode tree) {
        final JsonNode attributes = tree.get("data").get("attributes");
        return ImmutableUserInfo.builder()
                .email(attributes.get("email").asText())
                .username(attributes.get("username").asText())
                .build();
    }

    private static List<Tenant.Namespace> readNamespaces(JsonNode tree) {
        return StreamSupport.stream(tree.get("data").get("attributes").get("namespaces").spliterator(), false)
                .map(namespaceJson -> ImmutableNamespace.builder()
                        .name(namespaceJson.get("name").asText())
                        .type(namespaceJson.get("type").asText())
                        .clusterUrl(namespaceJson.get("cluster-url").asText())
                        .clusterConsoleUrl(namespaceJson.get("cluster-console-url").asText())
                        .build())
                .collect(Collectors.toList());
    }

    private static Space readSpace(final JsonNode tree) {
        final JsonNode data = tree.get("data");
        final JsonNode attributes = data.get("attributes");
        return ImmutableSpace.builder()
                .id(data.get("id").textValue())
                .name(attributes.get("name").textValue())
                .build();
    }

    private static void validateCodeBaseResponse(final String spaceId, final URI repositoryCloneUri, final Response response) {
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
    }
}
