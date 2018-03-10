package io.fabric8.launcher.web.producers;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.spi.Application;

import static io.fabric8.launcher.core.spi.Application.ApplicationLiteral.of;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.valueOf;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class MissionControlProducer {

    private static final String HEADER = "X-App";

    private static final String DEFAULT_APP = "launcher";

    @Produces
    @RequestScoped
    MissionControl getIdentityProvider(HttpServletRequest request, @Any Instance<MissionControl> instance) {
        // If X-App is not specified, assume fabric8-launcher
        String app = Objects.toString(request.getHeader(HEADER), DEFAULT_APP).toUpperCase();
        Application.ApplicationType type;
        try {
            type = valueOf(app);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Header 'X-App' has an invalid value: " + app);
        }
        Instance<MissionControl> missionControls = instance.select(MissionControl.class, of(type));
        if (missionControls.isUnsatisfied()) {
            throw new IllegalArgumentException("MissionControl not found for " + app);
        }
        MissionControl missionControl = missionControls.get();
        return missionControl;
    }
}