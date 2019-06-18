package io.fabric8.launcher.service.git;

import io.fabric8.launcher.service.git.api.GitServiceConfig;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

import static io.fabric8.launcher.base.http.Requests.APPLICATION_JSON;
import static okhttp3.RequestBody.create;

@ApplicationScoped
public class OAuthTokenProviderImpl implements OAuthTokenProvider {

    private final OkHttpClient client;

    private final KeyPair keyPair;
    private final Cipher cipher;

    OAuthTokenProviderImpl() throws NoSuchAlgorithmException, NoSuchPaddingException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        cipher = Cipher.getInstance("RSA");
        keyPair = keyPairGenerator.generateKeyPair();
        client = new OkHttpClient.Builder().build();
    }

    @Override
    public String getToken(String code, GitServiceConfig config) {
        String token = fetchAccessToken(code, config);
        return encodeToken(token);
    }

    private String encodeToken(String token) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            return Base64.getEncoder().encodeToString(cipher.doFinal(token.getBytes(StandardCharsets.UTF_8)));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException("Could not encrypt access token");
        }
    }

    private String fetchAccessToken(String code, GitServiceConfig config) {
        JsonObject object = Json.createObjectBuilder()
                .add("client_id", config.getServerProperties().get("clientId"))
                .add("client_secret", config.getServerProperties().get("clientSecret"))
                .add("code", code).build();
        StringWriter writer = new StringWriter();
        Json.createWriter(writer).write(object);
        Request request = new Request.Builder()
                .url(config.getServerProperties().get("oauthUrl"))
                .post(create(APPLICATION_JSON, writer.toString())).build();

        Response response;
        try {
            response = client.newCall(request).execute();
            return parseResult(Objects.requireNonNull(response.body()).string());
        } catch (IOException e) {
            throw new RuntimeException("Could not fetch access token", e);
        }
    }

    private String parseResult(String response) {
        HttpUrl httpUrl = HttpUrl.parse("http://dummy.com/?" + response);
        return Objects.requireNonNull(httpUrl).queryParameter("access_token");
    }

    public String decryptToken(String encryptedToken) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedToken)));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException("Could not decrypt access token", e);
        }
    }
}
