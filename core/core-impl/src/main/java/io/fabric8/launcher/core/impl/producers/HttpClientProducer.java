package io.fabric8.launcher.core.impl.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.fabric8.launcher.base.http.HttpClient;
import org.eclipse.microprofile.context.ManagedExecutor;

@ApplicationScoped
public class HttpClientProducer {

    @Produces
    @Singleton
    public HttpClient produceHttpClient(final ManagedExecutor executorService) {
        return HttpClient.create(executorService);
    }
}
