package io.fabric8.launcher.web.providers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class RuntimeParamConverter implements ParamConverter<Runtime> {

    // Cannot use constructor-type injection (gives NPE in CdiInjectorFactory)
    @Inject
    private Instance<RhoarBoosterCatalog> catalogInstance;

    @Override
    public Runtime fromString(String runtimeId) {
        if (runtimeId == null) {
            throw new IllegalArgumentException("Runtime ID is required");
        } else {
            RhoarBoosterCatalog catalog = catalogInstance.get();
            return catalog.getRuntimes().stream()
                    .filter(runtime -> runtime.getId().equals(runtimeId))
                    .findFirst()
                    .orElseThrow(() -> {
                        Response response = Response.status(Response.Status.BAD_REQUEST)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .entity(createArrayBuilder()
                                                .add(createObjectBuilder()
                                                             .add("message", "Runtime does not exist: " + runtimeId))
                                                .build())
                                .build();
                        return new WebApplicationException(response);
                    });
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
