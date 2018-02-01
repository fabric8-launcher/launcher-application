package io.fabric8.launcher.web.providers;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.core.spi.IdentityProvider;

import static io.fabric8.launcher.core.spi.Application.ApplicationLiteral.of;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */

public class IdentityProviderProducer {

    private static final String APP_HEADER = "X-App";

    private static final String DEFAULT_APP = "fabric8-launcher";

    @Produces
    @RequestScoped
    IdentityProvider getIdentityProvider(HttpServletRequest request, Instance<IdentityProvider> identities) {
        // If X-App is not specified, assume fabric8-launcher
        String app = Objects.toString(request.getHeader(APP_HEADER), DEFAULT_APP);
        // Because DefaultIdentityProvider is @Default, this call will never fail
        return identities.select(of(app)).get();
    }
}
