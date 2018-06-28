package io.fabric8.launcher.osio.client;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.osio.OsioConfigs;
import okhttp3.Request;
import okhttp3.RequestBody;

import static io.fabric8.launcher.base.http.Requests.securedRequest;

import static io.fabric8.utils.URLUtils.pathJoin;
import static java.util.Objects.requireNonNull;

@RequestScoped
public class AnalyticsClient {


    private final TokenIdentity authorization;

    private final HttpClient httpClient;

    @Inject
    public AnalyticsClient(final TokenIdentity authorization, HttpClient httpClient) {
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
    protected AnalyticsClient() {
        this.authorization = null;
        this.httpClient = null;
    }

    public boolean analyticsRequest(String path, RequestBody body) {
        final Request request = newAuthorizedRequestBuilder(path)
                .post(body)
                .build();
        final boolean response = httpClient.execute(request);
        return response;
    }

    private Request.Builder newAuthorizedRequestBuilder(final String path) {
        return securedRequest(authorization)
                .url(pathJoin(OsioConfigs.getAnalyticsUrl(), path));
    }

}
