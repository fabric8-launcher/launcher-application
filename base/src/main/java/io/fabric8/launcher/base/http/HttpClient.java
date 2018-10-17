package io.fabric8.launcher.base.http;

import java.io.IOException;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.JsonUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static java.util.Objects.requireNonNull;

/**
 * Executes HTTP requests
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class HttpClient {

    private final OkHttpClient client;

    private HttpClient(final OkHttpClient client) {
        this.client = requireNonNull(client, "client must be specified.");
    }

    /**
     * Constructs a {@link HttpClient} object
     */
    public static HttpClient create() {
        return create(null);
    }

    /**
     * Utility function reading content of the body
     * @param body Response stream from the server
     * @return "null" so that it can be understood by Json parser as null node
     * @throws IOException
     */
    public static String getContent(ResponseBody body) throws IOException {
        String content = "null";
        if (body == null) {
            return content;
        }
        final String bodyAsString = body.string();
        if (bodyAsString != null && !bodyAsString.trim().isEmpty()) {
            // with `null` it will become proper JsonNode
            content = bodyAsString;
        }
        return content;
    }

    /**
     * Constructs a {@link HttpClient} object by using the provided {@link ExecutorService} (which can be null) to make async calls.
     *
     * If not provided, this class will create a ThreadPoolExecutor to make async calls.
     *
     * @param executorService the nullable {@link ExecutorService}
     */
    public static HttpClient create(@Nullable final ExecutorService executorService) {
        return new HttpClient(createClient(executorService));
    }

    /**
     * Use this method only in places where an {@link OkHttpClient} instance is required
     *
     * @return the managed {@link OkHttpClient} instance.
     */
    public OkHttpClient getClient() {
        return client;
    }

    @Nullable
    @SuppressWarnings("squid:S1192")
    public <T> T executeAndMap(Request request, Function<Response, T> mapFunction) {
        try (final Response response = client.newCall(request).execute()) {
            return mapFunction.apply(response);
        } catch (IOException e) {
            throw new HttpException("Error while executing request", e);
        }
    }

    public void executeAndConsume(Request request, Consumer<Response> consumer) {
        try (final Response response = client.newCall(request).execute()) {
            consumer.accept(response);
        } catch (IOException e) {
            throw new HttpException("Error while executing request", e);
        }
    }

    public boolean execute(Request request) {
        try (final Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            throw new HttpException("Error while executing request", e);
        }
    }

    public <T> Optional<T> executeAndParseJson(Request request, Function<JsonNode, T> jsonNodeFunction) {
        try (Response response = client.newCall(request).execute()) {
            return parseJson(jsonNodeFunction, response);
        } catch (IOException e) {
            throw new HttpException("Error while executing request", e);
        }
    }

    public <T> CompletableFuture<T> executeAndMapAsync(Request request, Function<Response, T> mapFunction) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    future.complete(mapFunction.apply(response));
                } catch (@SuppressWarnings("squid:S1181") final Throwable t) {
                    future.completeExceptionally(t);
                } finally {
                    response.close();
                }
            }
        });
        return future;
    }

    public <T> CompletableFuture<Optional<T>> executeAndParseJsonAsync(Request request, final Function<JsonNode, T> jsonNodeFunction) {
        final CompletableFuture<Optional<T>> future = new CompletableFuture<>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    future.complete(parseJson(jsonNodeFunction, response));
                } catch (@SuppressWarnings("squid:S1181") final Throwable e) {
                    future.completeExceptionally(e);
                } finally {
                    response.close();
                }
            }
        });
        return future;
    }

    private <T> Optional<T> parseJson(Function<JsonNode, T> jsonNodeFunction, Response response) throws IOException {

        try (final ResponseBody body = response.body()) {
            if (response.isSuccessful()) {
                if (body == null || jsonNodeFunction == null) {
                    return Optional.empty();
                }
                String bodyString = body.string();
                if (bodyString == null || bodyString.isEmpty()) {
                    return Optional.empty();
                }
                JsonNode tree = JsonUtils.readTree(bodyString);
                return Optional.ofNullable(jsonNodeFunction.apply(tree));
            } else if (response.code() == 404) {
                return Optional.empty();
            }
            final String details = body != null ? body.string() : "No details";
            throw new HttpException(response.code(), String.format("HTTP Error %s: %s.", response.code(), details));
        }
    }

    private static final TrustManager[] trustAllCerts = {
            new X509ExtendedTrustManager() {

                @Override
                public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) {
                    //noop
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) {
                    //noop
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s, final Socket socket) {
                    //noop
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s, final Socket socket) {
                    //noop
                }

                @Override
                public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s, final SSLEngine sslEngine) {
                    //noop
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s, final SSLEngine sslEngine) {
                    //noop
                }
            }
    };

    private static OkHttpClient createClient(@Nullable ExecutorService executorService) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        } catch (Exception e) {
            //ignore
        }
        builder.hostnameVerifier((host, session) -> host.equalsIgnoreCase(session.getPeerHost()));
        if (executorService != null) {
            builder.dispatcher(new Dispatcher(executorService));
        }
        return builder.build();
    }

}