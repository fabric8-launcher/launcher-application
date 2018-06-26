package io.fabric8.launcher.osio.client;


import java.io.IOException;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.http.HttpException;
import io.fabric8.launcher.base.identity.TokenIdentity;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static io.fabric8.launcher.base.http.Requests.securedRequest;

import static io.fabric8.utils.URLUtils.pathJoin;
import static java.util.Objects.requireNonNull;

@RequestScoped
public class AnalyticsClient {


    private final TokenIdentity authorization;

    private final HttpClient httpClient;
    private static final String analyticsUrl = EnvironmentSupport.getRequiredEnvVarOrSysProp("F8A_ANALYTICS_RECOMMENDER_API_URL");

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

    public Response analyticsRequest(String path, RequestBody body) {
        final Request request = newAuthorizedRequestBuilder(path)
                .post(body)
                .build();
        Response response = null;

        try {
            response = httpClient.getClient().newCall(request).execute();
            if (!response.isSuccessful()) {
                String message = response.message();
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    message = responseBody.string();
                }
                throw new HttpException(response.code(), message);
            }
        } catch (IOException io) {
            //ignore
        }
        return  response;
    }

    private Request.Builder newAuthorizedRequestBuilder(final String path) {
        return securedRequest(authorization)
                .url(pathJoin(analyticsUrl, path));
    }

}
