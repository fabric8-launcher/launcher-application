package io.fabric8.launcher.web.providers;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.web.cdi.NamedLiteral;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */

public class IdentityProviderProducer {

    private static final String APP_HEADER = "X-Application";

    private static final String DEFAULT_APP = "fabric8-launcher";

    @Inject
    private Instance<IdentityProvider> identities;

    @Produces
    @RequestScoped
    IdentityProvider getIdentityProvider(HttpServletRequest request) {
        String defaultAppName = getDefaultAppName(request);
        Instance<IdentityProvider> providers = identities.select(NamedLiteral.of(defaultAppName));
        if (providers.isUnsatisfied()) {
            throw new BadRequestException("App " + defaultAppName + " is not available");
        }
        return providers.get();

    }

    private String getDefaultAppName(HttpServletRequest request) {
        // TODO: Grab the identity from the application (Using X-App or Host name)
        String app = request.getHeader(APP_HEADER);
        if (app == null) {
            app = DEFAULT_APP;
        }
        return app;
    }
}
