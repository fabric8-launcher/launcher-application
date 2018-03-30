package io.fabric8.launcher.base.http;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.launcher.base.identity.Identity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Executes external requests
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public final class ExternalRequest {

    private ExternalRequest() {
        throw new IllegalAccessError("Utility class");
    }

    private static final OkHttpClient client = createClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Nullable
    public static <T> T executeAndMap(Request request, Function<Response, T> mapFunction) {
        try (final Response response = client.newCall(request).execute()) {
            return mapFunction.apply(response);
        } catch (IOException e) {
            throw new HttpException("Error while executing request", e);
        }
    }

    public static void executeAndConsume(Request request, Consumer<Response> consumer) {
        try (final Response response = client.newCall(request).execute()) {
            consumer.accept(response);
        } catch (IOException e) {
            throw new HttpException("Error while executing request", e);
        }
    }

    public static boolean execute(Request request) {
        try (final Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            throw new HttpException("Error while executing request", e);
        }
    }


    public static <T> Optional<T> executeAndParseJson(Request request, Function<JsonNode, T> jsonNodeFunction) {
        try (Response response = client.newCall(request).execute()) {
            final ResponseBody body = response.body();
            if (response.isSuccessful()) {
                if (body == null || jsonNodeFunction == null) {
                    return Optional.empty();
                }
                String bodyString = body.string();
                if (bodyString == null || bodyString.isEmpty()) {
                    return Optional.empty();
                }
                JsonNode tree = mapper.readTree(bodyString);
                return Optional.ofNullable(jsonNodeFunction.apply(tree));
            } else if (response.code() == 404) {
                return Optional.empty();
            } else {
                final String details = body != null ? body.string() : "No details";
                throw new HttpException(response.code(), String.format("%s: %s.", response.message(), details));
            }
        } catch (IOException e) {
            throw new HttpException("Error while executing request", e);
        }
    }

    private static final TrustManager[] trustAllCerts = {
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            }
    };

    /**
     * Request builder factory prepared with identity authorization
     *
     * @param identity the {@link Identity to use}
     * @return the {@link Request.Builder}
     */
    public static Request.Builder securedRequest(final Identity identity) {
        return new Request.Builder().header(identity.toRequestHeaderKey(), identity.toRequestHeaderValue());
    }

    private static OkHttpClient createClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        } catch (Exception e) {
            //ignore
        }
        builder.hostnameVerifier((host, session) -> true);
        return builder.build();
    }

}