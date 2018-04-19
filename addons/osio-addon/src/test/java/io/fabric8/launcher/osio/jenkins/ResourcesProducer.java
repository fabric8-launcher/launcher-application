package io.fabric8.launcher.osio.jenkins;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * This class allows Java EE @Resources to be @Injected using CDI
 */
@ApplicationScoped
final class ResourcesProducer {

    @Resource
    @Produces
    private ManagedExecutorService managedExecutorService;

}
