package io.fabric8.launcher.web.providers;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.GitOperations;
import io.fabric8.launcher.core.spi.OpenShiftOperations;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class OperationsProducer {

    @Produces
    @RequestScoped
    GitOperations produceOperations(Instance<GitOperations> instances, Application application) {
        return instances.select(application).get();
    }

    @Produces
    @RequestScoped
    OpenShiftOperations produceOpenShiftOperations(Instance<OpenShiftOperations> instances, Application application) {
        return instances.select(application).get();
    }
}
