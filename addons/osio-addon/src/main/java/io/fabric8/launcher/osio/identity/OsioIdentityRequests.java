package io.fabric8.launcher.osio.identity;

import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.http.ExternalRequest;
import io.fabric8.launcher.base.identity.IdentityHelper;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.osio.EnvironmentVariables;
import okhttp3.Request;

public final class OsioIdentityRequests {

    private OsioIdentityRequests() {
        throw new IllegalAccessError("Helper class");
    }

    public static Optional<String> getServiceToken(final TokenIdentity osioToken, final String serviceName) {
        String authorizationHeader = IdentityHelper.createRequestAuthorizationHeaderValue(osioToken);
        Request gitHubTokenRequest = new Request.Builder()
                .url(EnvironmentVariables.ExternalServices.getTokenForURL() + serviceName)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .build();
        return ExternalRequest.readJson(gitHubTokenRequest, tree -> tree.get("access_token").asText());
    }

}
