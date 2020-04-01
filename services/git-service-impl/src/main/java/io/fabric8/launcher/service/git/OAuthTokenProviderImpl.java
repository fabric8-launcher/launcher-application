package io.fabric8.launcher.service.git;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.service.git.api.GitServiceConfig;
import okhttp3.HttpUrl;
import okhttp3.Request;

import static io.fabric8.launcher.base.http.Requests.APPLICATION_JSON;
import static okhttp3.RequestBody.create;

public class OAuthTokenProviderImpl implements OAuthTokenProvider {

    private final HttpClient client;

    OAuthTokenProviderImpl(HttpClient client) {
        this.client = client;
    }

    @Override
    public String getToken(String code, GitServiceConfig config) {
        String token;
        try {
            token = fetchAccessToken(code, config).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
        return token;
    }

    private CompletableFuture<String> fetchAccessToken(String code, GitServiceConfig config) {
        ObjectNode object = JsonUtils.createObjectNode()
                .put("client_id", config.getClientProperties().get("clientId"))
                .put("client_secret", config.getServerProperties().get("clientSecret"))
                .put("code", code);
        Request request = new Request.Builder()
                .url(config.getServerProperties().get("oauthUrl"))
                .post(create(APPLICATION_JSON, object.toString())).build();

        return client.executeAndMapAsync(request, response -> {
            try {
                return parseResult(Objects.requireNonNull(response.body()).string());
            } catch (IOException e) {
                throw new RuntimeException("Could not fetch access token", e);
            }
        });
    }

    private String parseResult(String response) {
        HttpUrl httpUrl = HttpUrl.parse("http://dummy.com/?" + response);
        return Objects.requireNonNull(httpUrl).queryParameter("access_token");
    }
}