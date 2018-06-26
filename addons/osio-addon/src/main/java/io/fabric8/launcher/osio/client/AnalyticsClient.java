package io.fabric8.launcher.osio.client;


import java.io.IOException;

import java.util.Objects;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.http.HttpException;
import io.fabric8.launcher.base.identity.TokenIdentity;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

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

    public void Request(String path, MediaType CONTENT_TYPE, String body) {
        final Request request = newAuthorizedRequestBuilder(path)
                    .post(RequestBody.create(CONTENT_TYPE, Objects.toString(body, "")))
                    .build();

        httpClient.executeAndConsume(request, response -> {
            if (!response.isSuccessful()) {
                String message = response.message();
                try {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        message = responseBody.string();
                    }
                } catch (IOException io) {
                    // ignore
                }
                throw new HttpException(response.code(), message);
            }
        });
    }

    private Request.Builder newAuthorizedRequestBuilder(final String path) {
        final String analyticsUrl = System.getenv("F8A_ANALYTICS_RECOMMENDER_API_URL");
        if (analyticsUrl == null) {
            throw new IllegalStateException("ENV variable F8A_ANALYTICS_RECOMMENDER_API_URL is not set");
        }
        return securedRequest(authorization)
                .url(pathJoin(analyticsUrl, path));
    }

}
