package io.fabric8.launcher.osio.jenkins;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.http.ExternalRequest;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.OsioConfigs;
import io.fabric8.launcher.osio.client.Tenant;
import okhttp3.Request;
import okhttp3.RequestBody;

import static io.fabric8.utils.URLUtils.pathJoin;

@RequestScoped
public class JenkinsGitCredentials {
    public JenkinsGitCredentials() {
    }

    JenkinsGitCredentials(String authorization, String gitToken) {
        this.authorization = authorization;
        this.gitToken = gitToken;
    }

    private static final String GET_URL = pathJoin(OsioConfigs.getJenkinsUrl(), "/credentials/store/system/domain/_/credentials/cd-github/");

    private static final String CREATE_URL = pathJoin(OsioConfigs.getJenkinsUrl(), "/credentials/store/system/domain/_/createCredentials");

    private String authorization;

    private String gitToken;

    @Inject
    private Tenant tenant;

    @Inject
    private IdentityProvider identityProvider;

    @PostConstruct
    public void init() {
        authorization = tenant.getIdentity().getToken();
        Identity identity = identityProvider.getIdentity(IdentityProvider.ServiceType.GITHUB, authorization)
                .orElseThrow(() -> new IllegalStateException("Invalid GITHUB token"));
        identity.accept(new IdentityVisitor() {

            @Override
            public void visit(TokenIdentity token) {
                JenkinsGitCredentials.this.gitToken = token.getToken();
            }
        });
    }

    public void ensureCredentials(String gitUserName) {
        if (!credentialsExist()) {
            createCredentials(gitUserName);
        }
    }

    private boolean credentialsExist() {
        Request getRequest = new Request.Builder()
                .url(GET_URL)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .build();

        return ExternalRequest.execute(getRequest, okhttp3.Response::isSuccessful);
    }

    private void createCredentials(String gitUserName) {

        JsonObjectBuilder credentials = Json.createObjectBuilder();
        credentials.add("", 0);
        credentials.add("credentials", Json.createObjectBuilder()
                .add("scope", "GLOBAL")
                .add("id", "cd-github")
                .add("username", gitUserName)
                .add("password", gitToken)
                .add("description", "fabric8 CD credentials for github")
                .add("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
        );

        Request request = new Request.Builder()
                .url(CREATE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .post(RequestBody.create(okhttp3.MediaType.parse("application/json"), credentials.build().toString()))
                .build();

        ExternalRequest.execute(request, response -> {
            if (!response.isSuccessful()) {
                throw new IllegalStateException(String.format("Failed to create credentials %s. Status: %d message: %s", CREATE_URL, response.code(), response.message()));
            }
            return response;
        });
    }
}
