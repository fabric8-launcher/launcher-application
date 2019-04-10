package io.fabric8.launcher.web.providers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.ext.ParamConverter;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class RuntimeParamConverter implements ParamConverter<Runtime> {

    // Cannot use constructor-type injection (gives NPE in CdiInjectorFactory)
    @Inject
    Instance<RhoarBoosterCatalog> catalogInstance;

    @Override
    public Runtime fromString(String runtimeId) {
        if (runtimeId == null) {
            throw new IllegalArgumentException("Runtime ID is required");
        } else {
            RhoarBoosterCatalog catalog = catalogInstance.get();
            return catalog.getRuntimes().stream()
                    .filter(runtime -> runtime.getId().equals(runtimeId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Runtime does not exist: " + runtimeId));
        }
    }

    @Override
    public String toString(Runtime value) {
        if (value == null) {
            throw new IllegalArgumentException("Runtime is required");
        }
        return value.getId();
    }
}
