package io.fabric8.launcher.osio.jenkins;

import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.http.ExternalRequest;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.OsioConfigs;
import io.fabric8.launcher.osio.client.Tenant;
import io.fabric8.utils.Strings;
import okhttp3.Request;
import okhttp3.RequestBody;

import static io.fabric8.launcher.base.identity.IdentityHelper.createRequestAuthorizationHeaderValue;
import static io.fabric8.utils.URLUtils.pathJoin;

@Dependent
public class JenkinsGitCredentials {

    private static final String GET_URL = pathJoin(OsioConfigs.getJenkinsUrl(), "/credentials/store/system/domain/_/credentials/cd-github/");

    private static final String CREATE_URL = pathJoin(OsioConfigs.getJenkinsUrl(), "/credentials/store/system/domain/_/createCredentials");


    private final Tenant tenant;

    private final IdentityProvider identityProvider;

    @Inject
    public JenkinsGitCredentials(final Tenant tenant, final IdentityProvider identityProvider) {
        this.tenant = tenant;
        this.identityProvider = identityProvider;
    }

    public void ensureCredentials(String gitUserName) {
        if (!credentialsExist()) {
            createCredentials(gitUserName);
        }
    }

    private boolean credentialsExist() {
        Request getRequest = new Request.Builder()
                .url(GET_URL)
                .header(HttpHeaders.AUTHORIZATION, createRequestAuthorizationHeaderValue(tenant.getIdentity()))
                .build();

        return ExternalRequest.execute(getRequest, okhttp3.Response::isSuccessful);
    }

    private void createCredentials(String gitUserName) {
        final String passwordOrToken = getGitPassword();
        JsonObjectBuilder credentials = Json.createObjectBuilder();
        credentials.add("", 0);
        credentials.add("credentials", Json.createObjectBuilder()
                .add("scope", "GLOBAL")
                .add("id", "cd-github")
                .add("username", gitUserName)
                .add("password", passwordOrToken)
                .add("description", "fabric8 CD credentials for github")
                .add("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
        );

        Request request = new Request.Builder()
                .url(CREATE_URL)
                .header(HttpHeaders.AUTHORIZATION, createRequestAuthorizationHeaderValue(tenant.getIdentity()))
                .post(RequestBody.create(okhttp3.MediaType.parse("application/json"), credentials.build().toString()))
                .build();

        ExternalRequest.execute(request, response -> {
            if (!response.isSuccessful()) {
                throw new IllegalStateException(String.format("Failed to create credentials %s. Status: %d message: %s", CREATE_URL, response.code(), response.message()));
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
}