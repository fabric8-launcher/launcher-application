package io.fabric8.launcher.core.impl.producers;

import java.util.concurrent.ExecutorService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.base.http.HttpClient;

@ApplicationScoped
public final class HttpClientProducer {

    @Produces
    @ApplicationScoped
    public HttpClient produceHttpClient(final ExecutorService executorService) {
        return HttpClient.create(executorService);
    }
}
