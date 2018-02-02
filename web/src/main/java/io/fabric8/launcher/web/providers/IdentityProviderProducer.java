package io.fabric8.launcher.web.providers;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;

import static io.fabric8.launcher.core.spi.Application.ApplicationLiteral.of;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.valueOf;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class IdentityProviderProducer {

    private static final String HEADER = "X-App";

    private static final String DEFAULT_APP = "launcher";

    @Produces
    @RequestScoped
    IdentityProvider getIdentityProvider(HttpServletRequest request, Instance<IdentityProvider> identities) {
        // If X-App is not specified, assume fabric8-launcher
        String app = Objects.toString(request.getHeader(HEADER), DEFAULT_APP).toUpperCase();
        Application.ApplicationType type = valueOf(app);
        return identities.select(of(type)).get();
    }
}