package io.fabric8.launcher.service.git.gitlab;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.service.git.OAuthTokenProvider;
import io.fabric8.launcher.service.git.api.GitServiceConfig;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GitLabOAuthTokenProviderImpl implements OAuthTokenProvider {

    private final HttpClient client;

    public GitLabOAuthTokenProviderImpl(HttpClient client) {
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
        RequestBody formBody = new FormBody.Builder()
                .add("client_id", config.getClientProperties().get("clientId"))
                .add("client_secret", config.getServerProperties().get("clientSecret"))
                .add("code", code)
                .add("grant_type", "authorization_code")
                .add("redirect_uri", config.getClientProperties().get("redirectUri"))
                .build();
        Request request = new Request.Builder()
                .url(config.getServerProperties().get("oauthUrl"))
                .post(formBody)
                .build();

        return client.executeAndMapAsync(request, response -> {
            try {
                ObjectNode res = parseResult(Objects.requireNonNull(response.body()).string());
                if (response.code() == 200) {
                    return Objects.requireNonNull(res.get("access_token")).toString();
                } else {
                    throw new RuntimeException("Error fetching access token: " + res.get("error_description"));
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not fetch access token", e);
            }
        });
    }

    private ObjectNode parseResult(String response) throws IOException {
        JsonNode result = JsonUtils.readTree(response);
        if (result.isObject()) {
            return (ObjectNode) result;
        } else {
            throw new RuntimeException("Invalid result fetching access token");
        }
    }
}