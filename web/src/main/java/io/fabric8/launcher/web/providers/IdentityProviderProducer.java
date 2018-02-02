package io.fabric8.launcher.web.providers;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class IdentityProviderProducer {

    private static final String HEADER = "X-App";

    private static final String DEFAULT_APP = "fabric8-launcher";

    @Produces
    @RequestScoped
    IdentityProvider getIdentityProvider(HttpServletRequest request, Instance<IdentityProvider> identities) {
        // If X-App is not specified, assume fabric8-launcher
        String app = Objects.toString(request.getHeader(HEADER), DEFAULT_APP);
        return identities.select(Application.ApplicationLiteral.of(app)).get();
    }
}