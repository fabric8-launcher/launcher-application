package io.fabric8.launcher.web.producers;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
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
    IdentityProvider getIdentityProvider(HttpServletRequest request, @Any Instance<IdentityProvider> identities) {
        // If X-App is not specified, assume fabric8-launcher
        String app = Objects.toString(request.getHeader(HEADER), DEFAULT_APP).toUpperCase();
        Application.ApplicationType type;
        try {
            type = valueOf(app);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Header 'X-App' has an invalid value: " + app);
        }
        Instance<IdentityProvider> identityProviders = identities.select(IdentityProvider.class, of(type));
        if (identityProviders.isUnsatisfied()) {
            throw new IllegalArgumentException("Identity provider not found for " + app);
        }
        IdentityProvider identityProvider = identityProviders.get();
        return identityProvider;
    }
}