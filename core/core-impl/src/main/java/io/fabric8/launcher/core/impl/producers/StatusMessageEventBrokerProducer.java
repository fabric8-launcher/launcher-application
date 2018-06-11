package io.fabric8.launcher.core.impl.producers;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.core.api.events.StatusMessageEventBroker;
import io.fabric8.launcher.core.impl.events.ArtemisStatusMessageEventBroker;
import io.fabric8.launcher.core.impl.events.StatusMessageEventBrokerImpl;

import static io.fabric8.launcher.base.EnvironmentSupport.getEnvVarOrSysProp;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class StatusMessageEventBrokerProducer {

    private static final Logger logger = Logger.getLogger(StatusMessageEventBrokerProducer.class.getName());

    @Produces
    @ApplicationScoped
    public StatusMessageEventBroker produceEventBroker() {
        String artemisUrl = getEnvVarOrSysProp("ARTEMIS_URL");
        if (artemisUrl == null) {
            logger.info("Handling status messages through " + StatusMessageEventBrokerImpl.class.getSimpleName());
            return new StatusMessageEventBrokerImpl();
        } else {
            String user = getEnvVarOrSysProp("ARTEMIS_USER");
            String password = getEnvVarOrSysProp("ARTEMIS_PASSWORD");
            logger.info("Handling status messages through " + ArtemisStatusMessageEventBroker.class.getSimpleName());
            return new ArtemisStatusMessageEventBroker(artemisUrl, user, password);
        }
    }

    public void destroyEventBroker(@Disposes StatusMessageEventBroker broker) {
        // Ensure this is called
        broker.close();
    }
}
