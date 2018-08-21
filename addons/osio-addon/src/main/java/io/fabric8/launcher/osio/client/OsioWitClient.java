package io.fabric8.launcher.osio.client;

import static io.fabric8.launcher.base.http.Requests.securedRequest;
import static io.fabric8.launcher.base.http.Requests.urlEncode;
import static io.fabric8.launcher.osio.OsioConfigs.getAuthUrl;
import static io.fabric8.launcher.osio.OsioConfigs.getWitUrl;
import static io.fabric8.utils.URLUtils.pathJoin;
import static java.util.Objects.requireNonNull;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;

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
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.http.HttpException;
import io.fabric8.launcher.base.identity.TokenIdentity;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Client to request Osio wit api
 */
@RequestScoped
public class OsioWitClient {

	protected static final String ERROR_HTTP_CLIENT_MUST_BE_SPECIFIED = "httpClient must be specified"; //$NON-NLS-1$
	protected static final String ERROR_AUTHORIZATION_MUST_BE_SPECIFIED = "authorization must be specified."; //$NON-NLS-1$

	private static final String ERROR_NAMESPACES_NOT_FOUND = "Namespaces not found"; //$NON-NLS-1$
	private static final String ERROR_USER_INFO_NOT_FOUND = "UserInfo not found"; //$NON-NLS-1$
	private static final String ERROR_CREATING_SPACE = "Error while creating space with name:"; //$NON-NLS-1$

	private static final String SERVICES = "/services"; //$NON-NLS-1$
	private static final String API_USER = "/api/user"; //$NON-NLS-1$
	private static final String CODEBASES = "/codebases"; //$NON-NLS-1$
	private static final String API_SPACES = "/api/spaces/"; //$NON-NLS-1$
	private static final String APPLICATION_JSON = "application/json"; //$NON-NLS-1$

	private static final String USERNAME = "username"; //$NON-NLS-1$
	private static final String CLUSTER = "cluster"; //$NON-NLS-1$
	private static final String EMAIL = "email"; //$NON-NLS-1$
	private static final String NAMESPACES = "namespaces"; //$NON-NLS-1$
	private static final String TYPE = "type"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String ATTRIBUTES = "attributes"; //$NON-NLS-1$
	private static final String DATA = "data"; //$NON-NLS-1$
	private static final String DETAIL = "detail"; //$NON-NLS-1$
	private static final String ERRORS = "errors"; //$NON-NLS-1$

	private static final Logger logger = Logger.getLogger(OsioWitClient.class.getName());

	private final TokenIdentity authorization;
	private final HttpClient httpClient;

	@Inject
	public OsioWitClient(final TokenIdentity auth, HttpClient client) {
		this.authorization = requireNonNull(auth, ERROR_AUTHORIZATION_MUST_BE_SPECIFIED);
		this.httpClient = requireNonNull(client, ERROR_HTTP_CLIENT_MUST_BE_SPECIFIED);
	}

	/**
	 * no-args constructor used by CDI for proxying only but is subsequently
	 * replaced with an instance created using the above constructor.
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
		return ImmutableTenant.builder().userInfo(getUserInfo()).namespaces(getNamespaces())
				.identity(this.authorization).build();
	}

	/**
	 * Find the space for the given id
	 *
	 * @param spaceId the space id
	 * @return the {@link Optional<Space>}
	 */
	public Optional<Space> findSpaceById(final String spaceId) {
		final Request request = newAuthorizedRequestBuilder(getWitUrl(), API_SPACES + urlEncode(spaceId)).build();
		return this.httpClient.executeAndParseJson(request, OsioWitClient::readSpace);
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
				"{\"data\":{\"attributes\":{\"stackId\":\"%s\",\"type\":\"git\",\"url\":\"%s\"},\"type\":\"codebases\"}}",
				stackId, repositoryCloneUri);
		final Request request = newAuthorizedRequestBuilder(getWitUrl(), API_SPACES + spaceId + CODEBASES)
				.post(create(parse(APPLICATION_JSON), payload)).build();
		this.httpClient.executeAndConsume(request, r -> validateCodeBaseResponse(spaceId, repositoryCloneUri, r));
	}

	/**
	 * Create a space with the given name
	 *
	 * @param spaceName the space name
	 * @return the spaceId
	 */
	public Space createSpace(final String spaceName) {
		final String payload = String.format("{\"data\":{\"attributes\":{\"name\":\"%s\"},\"type\":\"spaces\"}}",
				spaceName);
		final Request request = newAuthorizedRequestBuilder(getWitUrl(), API_SPACES)
				.post(create(parse(APPLICATION_JSON), payload)).build();
		return this.httpClient.executeAndParseJson(request, OsioWitClient::readSpace)
				.orElseThrow(() -> new IllegalStateException(ERROR_CREATING_SPACE + spaceName));
	}

	/**
	 * Delete the space with the given id
	 *
	 * @param spaceId the spaceId
	 */
	public void deleteSpace(final String spaceId) {
		final Request request = newAuthorizedRequestBuilder(getWitUrl(), API_SPACES + urlEncode(spaceId)).delete()
				.build();
		this.httpClient.executeAndConsume(request, response -> {
			if (!response.isSuccessful()) {
				String message = response.message();
				try (ResponseBody body = response.body()) {
					if (body != null) {
						message = body.string();
					}
				} catch (IOException io) {
					logger.log(Level.WARNING, io.getMessage(), io);
				}
				throw new HttpException(response.code(), message);
			}
		});
	}

	private Tenant.UserInfo getUserInfo() {
		// Use the Auth URL because it has the Cluster attribute
		final Request userInfoRequest = newAuthorizedRequestBuilder(getAuthUrl(), API_USER).build();
		return this.httpClient.executeAndParseJson(userInfoRequest, OsioWitClient::readUserInfo)
				.orElseThrow(() -> new BadTenantException(ERROR_USER_INFO_NOT_FOUND));
	}

	private List<Tenant.Namespace> getNamespaces() {
		final Request namespacesRequest = newAuthorizedRequestBuilder(getWitUrl(), API_USER + SERVICES).build();
		return this.httpClient.executeAndParseJson(namespacesRequest, OsioWitClient::readNamespaces)
				.orElseThrow(() -> new BadTenantException(ERROR_NAMESPACES_NOT_FOUND));
	}

	private Request.Builder newAuthorizedRequestBuilder(final String url, final String path) {
		return securedRequest(this.authorization).url(pathJoin(url, path));
	}

	private static Tenant.UserInfo readUserInfo(JsonNode tree) {
		final JsonNode attributes = tree.get(DATA).get(ATTRIBUTES);
		return ImmutableUserInfo.builder().email(attributes.get(EMAIL).asText())
				.username(attributes.get(USERNAME).asText()).cluster(attributes.get(CLUSTER).asText()).build();
	}

	private static List<Tenant.Namespace> readNamespaces(JsonNode tree) {
		return StreamSupport.stream(tree.get(DATA).get(ATTRIBUTES).get(NAMESPACES).spliterator(), false)
				.map(namespaceJson -> ImmutableNamespace.builder().name(namespaceJson.get(NAME).asText())
						.type(namespaceJson.get(TYPE).asText()).clusterUrl(namespaceJson.get("cluster-url").asText())
						.clusterConsoleUrl(namespaceJson.get("cluster-console-url").asText()).build())
				.collect(Collectors.toList());
	}

	private static Space readSpace(final JsonNode tree) {
		final JsonNode data = tree.get(DATA);
		final JsonNode attributes = data.get(ATTRIBUTES);
		return ImmutableSpace.builder().id(data.get(ID).textValue()).name(attributes.get(NAME).textValue()).build();
	}

	private static void validateCodeBaseResponse(final String spaceId, final URI repositoryCloneUri,
			final Response response) {
		if (response.code() == 409) {
			// Duplicate. This can be ignored for now as there is no connection in the
			// 'beginning' of the wizard to
			// verify what is in the codebase API
			logger.log(Level.FINE,
					() -> "Duplicate codebase for spaceId " + spaceId + " and repository " + repositoryCloneUri);
		} else if (!response.isSuccessful()) {
			assert response.body() != null;
			String message = response.message();
			try {
				String body = response.body().string();
				JsonNode errors = JsonUtils.readTree(body).get(ERRORS);
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw, true);
				for (JsonNode error : errors) {
					pw.println(error.get(DETAIL).asText());
				}
				message = sw.toString();
			} catch (IOException e) {
				logger.log(Level.WARNING, "Error while reading error from WIT", e);
			}
			throw new HttpException(response.code(), message);
		}
	}
}
