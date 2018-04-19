package io.fabric8.launcher.osio.client;


import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.utils.Strings;
import okhttp3.Request;

import static io.fabric8.launcher.base.http.Requests.securedRequest;
import static io.fabric8.launcher.osio.OsioConfigs.getJenkinsUrl;
import static io.fabric8.utils.URLUtils.pathJoin;
import static java.util.Objects.requireNonNull;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;

/**
 * Client to request Osio auth api
 */
@RequestScoped
public class OsioJenkinsClient {

    private final HttpClient httpClient;
    private final TokenIdentity authorization;

    @Inject
    public OsioJenkinsClient(final HttpClient httpClient,
                             final TokenIdentity authorization) {
        this.httpClient = requireNonNull(httpClient, "httpClient must be specified");
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
    protected OsioJenkinsClient() {
        this.authorization = null;
        this.httpClient = null;
    }

    /**
     * Ensure credentials exist for the specified git username
     *
     * @param gitUserName the git username
     * @param gitIdentity the git identity
     */
    public void ensureCredentials(final String gitUserName, final Identity gitIdentity) {
        if (!credentialsExist()) {
            createCredentials(gitUserName, gitIdentity);
        }
    }

    private boolean credentialsExist() {
        Request getRequest = newAuthorizedRequestBuilder("/credentials/store/system/domain/_/credentials/cd-github/")
                .build();
        return httpClient.execute(getRequest);
    }

    private void createCredentials(final String gitUserName, final Identity gitIdentity) {
        final String passwordOrToken = getGitPassword(gitIdentity);
        final String payload = Json.createObjectBuilder()
                .add("", 0)
                .add("credentials", Json.createObjectBuilder()
                        .add("scope", "GLOBAL")
                        .add("id", "cd-github")
                        .add("username", gitUserName)
                        .add("password", passwordOrToken)
                        .add("description", "fabric8 CD credentials for github")
                        .add("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                ).build().toString();

        Request request = newAuthorizedRequestBuilder("/credentials/store/system/domain/_/createCredentials")
                .post(create(parse("application/json"), payload))
                .build();

        httpClient.executeAndMap(request, response -> {
            if (!response.isSuccessful()) {
                throw new IllegalStateException(String.format("Failed to create credentials %s. Status: %d message: %s", request.url(), response.code(), response.message()));
            }
            return response;
        });
    }

    private String getGitPassword(final Identity gitIdentity) {
        final AtomicReference<String> passwordOrToken = new AtomicReference<>();
        gitIdentity.accept(new IdentityVisitor() {
            @Override
            public void visit(final TokenIdentity token) {
                passwordOrToken.set(token.getToken());
            }

            @Override
            public void visit(final UserPasswordIdentity userPassword) {
                passwordOrToken.set(userPassword.getPassword());
            }
        });
        if (Strings.isNullOrBlank(passwordOrToken.get())) {
            throw new IllegalStateException("Invalid git credentials");
        }
        return passwordOrToken.get();
    }

    private Request.Builder newAuthorizedRequestBuilder(final String path) {
        return securedRequest(authorization)
                .url(pathJoin(getJenkinsUrl(), path));
    }
}
