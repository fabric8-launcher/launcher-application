package io.fabric8.launcher.osio.client.impl;

import java.util.Optional;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.osio.client.api.OsioAuthClient;
import io.fabric8.launcher.osio.steps.WitSteps;
import okhttp3.Request;

import static io.fabric8.launcher.base.http.ExternalRequest.executeAndParseJson;
import static io.fabric8.launcher.osio.OsioConfigs.getAuthUrl;
import static io.fabric8.utils.URLUtils.pathJoin;

public final class OsioAuthClientImpl implements OsioAuthClient {
    private static final Logger LOG = Logger.getLogger(WitSteps.class.getName());

    private final String authorizationHeader;

    public OsioAuthClientImpl(final String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    @Override
    public Optional<String> getTokenForService(final String serviceName) {
        final Request gitHubTokenRequest = newAuthorizedRequestBuilder("/api/token?for=" + serviceName).build();
        return executeAndParseJson(gitHubTokenRequest, tree -> tree.get("access_token").asText());
    }

    private Request.Builder newAuthorizedRequestBuilder(final String path) {
        return new Request.Builder()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .url(pathJoin(getAuthUrl(), path));
    }

}
