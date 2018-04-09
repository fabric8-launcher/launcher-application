package io.fabric8.launcher.core.impl;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
final class ApplicationScopedResourcesProducer {

    @Resource
    @Produces
    private ManagedExecutorService managedExecutorService;

}
