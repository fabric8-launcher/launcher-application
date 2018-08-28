package io.fabric8.launcher.osio.client;


import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.http.HttpException;
import io.fabric8.launcher.base.identity.TokenIdentity;
import okhttp3.Request;
import okhttp3.ResponseBody;

import static io.fabric8.launcher.base.http.Requests.securedRequest;
import static io.fabric8.launcher.base.http.Requests.urlEncode;
import static io.fabric8.launcher.osio.OsioConfigs.getAuthUrl;
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

    private final HttpClient httpClient;

    @Inject
    public OsioWitClient(final TokenIdentity authorization, HttpClient httpClient) {
        this.authorization = requireNonNull(authorization, "authorization must be specified.");
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
    protected OsioWitClient() {
        this.authorization = null;
        this.httpClient = null;
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
        final Request request = newAuthorizedRequestBuilder(getWitUrl(), "/api/spaces/" + urlEncode(spaceId)).build();
        return httpClient.executeAndParseJson(request, OsioWitClient::readSpace);
    }

    /**
     * Create a code base with the specified repository
     *
     * @param spaceId            the spaceId
     * @param stackId            the stackId
     * @param repositoryCloneUri the repository clone {@link URI}
     */
    public String createCodeBase(final String spaceId, final String stackId, final URI repositoryCloneUri) {

        final String payload = String.format(
                "{\"data\":{\"attributes\":{\"stackId\":\"%s\",\"type\":\"git\",\"url\":\"%s\"},\"type\":\"codebases\"}}",
                stackId,
                repositoryCloneUri
        );
        final Request request = newAuthorizedRequestBuilder(getWitUrl(), "/api/spaces/" + spaceId + "/codebases")
                .post(create(parse("application/json"), payload))
                .build();

        return httpClient.executeAndMap(request, response -> {
            if (response.code() == 409) {
                // Duplicate. This can be ignored for now as there is no connection in the 'beginning' of the wizard to
                // verify what is in the codebase API
                logger.log(Level.FINE, () -> "Duplicate codebase for spaceId " + spaceId + " and repository " + repositoryCloneUri);
            } else {
                try {
                    final String body = Objects.requireNonNull(response.body()).string();
                    return JsonUtils.readTree(body).get("data").get("id").asText();
                } catch (IOException e) {
                    throw new IllegalStateException("Error while reading response [" + response.toString() + "]");
                }
            }
            return "unknown-codebase-id";
        });
    }

    /**
     * Create a space with the given name
     *
     * @param spaceName the space name
     * @return the spaceId
     */
    public Space createSpace(final String spaceName) {
        final String payload = String.format("{\"data\":{\"attributes\":{\"name\":\"%s\"},\"type\":\"spaces\"}}", spaceName);
        final Request request = newAuthorizedRequestBuilder(getWitUrl(), "/api/spaces")
                .post(create(parse("application/json"), payload))
                .build();
        return httpClient.executeAndParseJson(request, OsioWitClient::readSpace)
                .orElseThrow(() -> new IllegalStateException("Error while creating space with name:" + spaceName));
    }

    /**
     * Delete the space with the given id
     *
     * @param spaceId the spaceId
     */
    public void deleteSpace(final String spaceId) {
        final Request request = newAuthorizedRequestBuilder(getWitUrl(), "/api/spaces/" + urlEncode(spaceId))
                .delete()
                .build();
        httpClient.executeAndConsume(request, response -> {
            if (!response.isSuccessful()) {
                String message = response.message();
                try {
                    ResponseBody body = response.body();
                    if (body != null) {
                        message = body.string();
                    }
                } catch (IOException io) {
                    // ignore
                }
                throw new HttpException(response.code(), message);
            }
        });
    }

    private Tenant.UserInfo getUserInfo() {
        // Use the Auth URL because it has the Cluster attribute
        final Request userInfoRequest = newAuthorizedRequestBuilder(getAuthUrl(), "/api/user").build();
        return httpClient.executeAndParseJson(userInfoRequest, OsioWitClient::readUserInfo)
                .orElseThrow(() -> new BadTenantException("UserInfo not found"));
    }

    private List<Tenant.Namespace> getNamespaces() {
        final Request namespacesRequest = newAuthorizedRequestBuilder(getWitUrl(), "/api/user/services").build();
        return httpClient.executeAndParseJson(namespacesRequest, OsioWitClient::readNamespaces)
                .orElseThrow(() -> new BadTenantException("Namespaces not found"));
    }

    private Request.Builder newAuthorizedRequestBuilder(final String url, final String path) {
        return securedRequest(authorization)
                .url(pathJoin(url, path));
    }

    private static Tenant.UserInfo readUserInfo(JsonNode tree) {
        final JsonNode attributes = tree.get("data").get("attributes");
        return ImmutableUserInfo.builder()
                .email(attributes.get("email").asText())
                .username(attributes.get("username").asText())
                .cluster(attributes.get("cluster").asText())
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

}
