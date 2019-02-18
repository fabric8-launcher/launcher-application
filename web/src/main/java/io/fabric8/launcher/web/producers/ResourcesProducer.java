package io.fabric8.launcher.web.producers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

/**
 * This class allows Java EE @Resources to be @Injected using CDI
 */
@ApplicationScoped
public class ResourcesProducer {

    @Produces
    ExecutorService managedExecutorService = Executors.newCachedThreadPool();

    void destroy(@Disposes ExecutorService ex) {
        ex.shutdown();
    }

}
