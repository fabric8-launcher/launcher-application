package io.fabric8.launcher.osio.client;


import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;

import io.fabric8.launcher.base.http.Requests;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.utils.Strings;
import okhttp3.Request;

import static io.fabric8.launcher.base.http.Requests.securedRequest;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;
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

    private final TokenIdentity authorization;

    private final IdentityProvider identityProvider;

    private final Requests requests;


    @Inject
    public OsioJenkinsClient(final TokenIdentity authorization,
                             @Application(OSIO) final IdentityProvider identityProvider,
                             Requests requests) {
        this.authorization = requireNonNull(authorization, "authorization must be specified.");
        this.identityProvider = requireNonNull(identityProvider, "identityProvider must be specified.");
        this.requests = requireNonNull(requests, "requests must be specified");
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
        this.identityProvider = null;
        this.requests = null;
    }

    /**
     * Ensure credentials exist for the specified git username
     *
     * @param gitUserName the git username
     */
    public void ensureCredentials(final String gitUserName) {
        if (!credentialsExist()) {
            createCredentials(gitUserName);
        }
    }

    private boolean credentialsExist() {
        Request getRequest = newAuthorizedRequestBuilder("/credentials/store/system/domain/_/credentials/cd-github/")
                .build();
        return requests.execute(getRequest);
    }

    private void createCredentials(String gitUserName) {
        final String passwordOrToken = getGitPassword();
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

        requests.executeAndMap(request, response -> {
            if (!response.isSuccessful()) {
                throw new IllegalStateException(String.format("Failed to create credentials %s. Status: %d message: %s", request.url(), response.code(), response.message()));
            }
            return response;
        });
    }

    private String getGitPassword() {
        final Identity identity = identityProvider.getIdentity(IdentityProvider.ServiceType.GITHUB)
                .orElseThrow(() -> new IllegalStateException("Invalid GITHUB token"));
        final AtomicReference<String> passwordOrToken = new AtomicReference<>();
        identity.accept(new IdentityVisitor() {
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
