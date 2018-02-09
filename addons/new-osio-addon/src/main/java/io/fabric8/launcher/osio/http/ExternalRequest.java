package io.fabric8.launcher.osio.http;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.utils.Strings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Executes external requests
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ExternalRequest {


    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> Optional<T> readJson(Request request, Function<JsonNode, T> consumer) {
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body == null || consumer == null) {
                    return Optional.empty();
                }
                String bodyString = body.string();
                if (Strings.isNullOrBlank(bodyString)) {
                    return Optional.empty();
                }
                JsonNode tree = mapper.readTree(bodyString);
                return Optional.ofNullable(consumer.apply(tree));
            } else {
                throw new HttpException(response.code(), response.message());
            }
        } catch (IOException e) {
            throw new HttpException("Error while executing request", e);
        }
    }


    private static final OkHttpClient client = getClient();


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

    private static OkHttpClient getClient() {
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