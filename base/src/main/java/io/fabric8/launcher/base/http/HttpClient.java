package io.fabric8.launcher.base.http;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
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

/**
 * Executes HTTP requests
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class HttpClient {

    private final OkHttpClient client;

    /**
     * Shortcut to {@link HttpClient#HttpClient(ExecutorService)} passing <code>null</code> as the {@link ExecutorService}
     */
    public HttpClient() {
        this(null);
    }

    /**
     * Constructs a {@link HttpClient} object by using the provided {@link ExecutorService} (which can be null)
     *
     * @param executorService used in the async methods
     */
    @Inject
    public HttpClient(@Nullable ExecutorService executorService) {
        client = createClient(executorService);
    }


    @Nullable
    public <T> T executeAndMap(Request request, Function<Response, T> mapFunction) {
        try (final Response response = client.newCall(request).execute()) {
            return mapFunction.apply(response);
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
                future.complete(mapFunction.apply(response));
            }
        });
        return future;
    }

    public void executeAndConsume(Request request, Consumer<Response> consumer) {
        try (final Response response = client.newCall(request).execute()) {
            consumer.accept(response);
        } catch (IOException e) {
            throw new HttpException("Error while executing request", e);
        }
    }

    public void executeAndConsumeAsync(Request request, Consumer<Response> consumer) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // TODO: Handle failures
            }

            @Override
            public void onResponse(Call call, Response response) {
                consumer.accept(response);
            }
        });
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
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });
        return future;
    }

    private <T> Optional<T> parseJson(Function<JsonNode, T> jsonNodeFunction, Response response) throws IOException {
        final ResponseBody body = response.body();
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
        } else {
            final String details = body != null ? body.string() : "No details";
            throw new HttpException(response.code(), String.format("HTTP Error %s: %s.", response.code(), details));
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

    private static OkHttpClient createClient(@Nullable ExecutorService executorService) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        } catch (Exception e) {
            //ignore
        }
        builder.hostnameVerifier((host, session) -> true)
                .dispatcher(new Dispatcher(executorService));
        return builder.build();
    }

}