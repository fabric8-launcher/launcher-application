package io.fabric8.launcher.core.impl.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;
import io.fabric8.launcher.core.impl.events.StatusMessageEventBrokerImpl;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class StatusMessageEventBrokerProducer {

    @Produces
    @ApplicationScoped
    public StatusMessageEventBroker produceMessageBuffer() {
        return new StatusMessageEventBrokerImpl();
    }
}
