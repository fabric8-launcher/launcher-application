package io.fabric8.launcher.osio.client;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.http.ExternalRequest;
import io.fabric8.launcher.base.http.HttpException;
import io.fabric8.launcher.base.identity.TokenIdentity;
import okhttp3.Request;
import okhttp3.Response;

import static io.fabric8.launcher.base.http.ExternalRequest.executeAndParseJson;
import static io.fabric8.launcher.base.http.ExternalRequest.securedRequest;
import static io.fabric8.launcher.osio.OsioConfigs.getWitUrl;
import static io.fabric8.utils.URLUtils.pathJoin;
import static java.util.Objects.requireNonNull;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;

/**
 * Client to request Osio wit api
 */
@RequestScoped
public class OsioWitClient {

    private static final Logger logger = Logger.getLogger(OsioWitClient.class.getName());

    private final TokenIdentity authorization;

    @Inject
    public OsioWitClient(final TokenIdentity authorization) {
        this.authorization = requireNonNull(authorization, "authorization must be specified.");
    }

    /**
     * no-args constructor used by CDI for proxying only
     * but is subsequently replaced with an instance
     * created using the above constructor.
     *
     * @deprecated do not use this constructor
     */
    @Deprecated
    protected OsioWitClient() {
        this.authorization = null;
    }

    /**
     * Get the logged user
     *
     * @return the {@link Tenant}
     */
    public Tenant getTenant() {
        return ImmutableTenant.builder()
                .userInfo(getUserInfo())
                .namespaces(getNamespaces())
                .identity(authorization)
                .build();
    }

    /**
     * Find the space for the given id
     *
     * @param spaceId the space id
     * @return the {@link Optional<Space>}
     */
    public Optional<Space> findSpaceById(final String spaceId) {
        final Request request = newAuthorizedRequestBuilder("/api/spaces/" + spaceId).build();
        return executeAndParseJson(request, OsioWitClient::readSpace);
    }

    /**
     * Find the space for the given name
     *
     * @param tenantName the tenant name
     * @param spaceName  the space name
     * @return the {@link Optional<Space>}
     */
    public Optional<Space> findSpaceByName(final String tenantName, final String spaceName) {
        final Request request = newAuthorizedRequestBuilder("/api/namedspaces/" + tenantName + "/" + spaceName).build();
        return executeAndParseJson(request, OsioWitClient::readSpace);
    }

    /**
     * Create a code base with the specified repository
     *
     * @param spaceId            the spaceId
     * @param stackId            the stackId
     * @param repositoryCloneUri the repository clone {@link URI}
     */
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

    /**
     * Create a space with the given name
     *
     * @param spaceName the space name
     * @return the spaceId
     */
    public Space createSpace(final String spaceName) {
        final String payload = String.format("{\"data\":{\"attributes\":{\n\"name\":\"%s\"},\"type\":\"spaces\"}}", spaceName);
        final Request request = newAuthorizedRequestBuilder("/api/spaces")
                .post(create(parse("application/json"), payload))
                .build();
        return ExternalRequest.executeAndParseJson(request, OsioWitClient::readSpace)
                .orElseThrow(() -> new IllegalStateException("Error while creating space with name:" + spaceName));
    }

    /**
     * Delete the space with the given id
     *
     * @param spaceId the spaceId
     */
    public boolean deleteSpace(final String spaceId) {
        final Request request = newAuthorizedRequestBuilder("/api/spaces/" + spaceId)
                .delete()
                .build();
        return ExternalRequest.execute(request);
    }

    private Tenant.UserInfo getUserInfo() {
        final Request userInfoRequest = newAuthorizedRequestBuilder("/api/user").build();
        return executeAndParseJson(userInfoRequest, OsioWitClient::readUserInfo)
                .orElseThrow(() -> new BadTenantException("UserInfo not found"));
    }

    private List<Tenant.Namespace> getNamespaces() {
        final Request namespacesRequest = newAuthorizedRequestBuilder("/api/user/services").build();
        return executeAndParseJson(namespacesRequest, OsioWitClient::readNamespaces)
                .orElseThrow(() -> new BadTenantException("Namespaces not found"));
    }

    private Request.Builder newAuthorizedRequestBuilder(final String path) {
        return securedRequest(authorization)
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
            logger.log(Level.FINE, () -> "Duplicate codebase for spaceId " + spaceId + " and repository " + repositoryCloneUri);
        } else if (!response.isSuccessful()) {
            assert response.body() != null;
            String message = response.message();
            try {
                String body = response.body().string();
                JsonNode errors = JsonUtils.readTree(body).get("errors");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw, true);
                for (JsonNode error : errors) {
                    pw.println(error.get("detail").asText());
                }
                message = sw.toString();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error while reading error from WIT", e);
            }
            throw new HttpException(response.code(), message);
        }
    }
}
