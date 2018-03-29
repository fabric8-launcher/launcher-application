package io.fabric8.launcher.osio.client.impl;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.json.Json;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.http.ExternalRequest;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.client.api.OsioJenkinsClient;
import io.fabric8.launcher.osio.steps.WitSteps;
import io.fabric8.utils.Strings;
import okhttp3.Request;

import static io.fabric8.launcher.base.http.ExternalRequest.execute;
import static io.fabric8.launcher.osio.OsioConfigs.getJenkinsUrl;
import static io.fabric8.utils.URLUtils.pathJoin;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;

public final class OsioJenkinsClientImpl implements OsioJenkinsClient {
    private static final Logger LOG = Logger.getLogger(WitSteps.class.getName());

    private final String authorizationHeader;
    private final IdentityProvider identityProvider;

    public OsioJenkinsClientImpl(final String authorizationHeader, final IdentityProvider identityProvider) {
        this.authorizationHeader = authorizationHeader;
        this.identityProvider = identityProvider;
    }

    @Override
    public void ensureCredentials(final String gitUserName) {
        if (!credentialsExist()) {
            createCredentials(gitUserName);
        }
    }

    private boolean credentialsExist() {
        Request getRequest = newAuthorizedRequestBuilder("/credentials/store/system/domain/_/credentials/cd-github/")
                .build();
        return execute(getRequest);
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

        ExternalRequest.executeAndMap(request, response -> {
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
        return new Request.Builder()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .url(pathJoin(getJenkinsUrl(), path));
    }

}
