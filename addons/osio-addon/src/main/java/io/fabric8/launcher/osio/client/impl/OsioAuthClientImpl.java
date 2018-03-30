package io.fabric8.launcher.osio.client.impl;

import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.osio.client.api.OsioAuthClient;
import okhttp3.Request;

import static io.fabric8.launcher.base.http.ExternalRequest.executeAndParseJson;
import static io.fabric8.launcher.base.http.ExternalRequest.securedRequest;
import static io.fabric8.launcher.osio.OsioConfigs.getAuthUrl;
import static io.fabric8.utils.URLUtils.pathJoin;
import static java.util.Objects.requireNonNull;

@RequestScoped
public final class OsioAuthClientImpl implements OsioAuthClient {
    private static final Logger LOG = Logger.getLogger(OsioAuthClientImpl.class.getName());

    private final TokenIdentity authorization;

    @Inject
    public OsioAuthClientImpl(final TokenIdentity authorization) {
        this.authorization = requireNonNull(authorization, "authorization must be specified.");
    }

    @Override
    public Optional<String> getTokenForService(final String serviceName) {
        final Request gitHubTokenRequest = newAuthorizedRequestBuilder("/api/token?for=" + serviceName).build();
        return executeAndParseJson(gitHubTokenRequest, tree -> tree.get("access_token").asText());
    }

    private Request.Builder newAuthorizedRequestBuilder(final String path) {
        return securedRequest(authorization)
                .url(pathJoin(getAuthUrl(), path));
    }

}
