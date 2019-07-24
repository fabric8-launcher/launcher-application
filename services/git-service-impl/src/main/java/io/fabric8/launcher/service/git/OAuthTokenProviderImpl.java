package io.fabric8.launcher.service.git;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.service.git.api.GitServiceConfig;
import okhttp3.HttpUrl;
import okhttp3.Request;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.fabric8.launcher.base.http.Requests.APPLICATION_JSON;
import static okhttp3.RequestBody.create;

@Singleton
public class OAuthTokenProviderImpl implements OAuthTokenProvider {

    private final HttpClient client;

    private final KeyPair keyPair;
    private final Cipher cipher;

    @Inject
    OAuthTokenProviderImpl(HttpClient client) throws NoSuchAlgorithmException, NoSuchPaddingException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        cipher = Cipher.getInstance("RSA");
        keyPair = keyPairGenerator.generateKeyPair();
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
        return encodeToken(token);
    }

    @Override
    public String decryptToken(String encryptedToken) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedToken)));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException("Could not decrypt access token", e);
        }
    }

    private String encodeToken(String token) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            return Base64.getEncoder().encodeToString(cipher.doFinal(token.getBytes(StandardCharsets.UTF_8)));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException("Could not encrypt access token");
        }
    }

    private CompletableFuture<String> fetchAccessToken(String code, GitServiceConfig config) {
        ObjectNode object = JsonUtils.createObjectNode()
                .put("client_id", config.getServerProperties().get("clientId"))
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