package io.fabric8.launcher.web.producers;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.PublicKeyProvider;

import static io.fabric8.launcher.core.spi.Application.ApplicationLiteral.of;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.valueOf;

@ApplicationScoped
public class PublicKeyProviderProducer {

    private static final String HEADER = "X-App";

    private static final String DEFAULT_APP = "launcher";

    @Produces
    @RequestScoped
    PublicKeyProvider getIdentityProvider(HttpServletRequest request, @Any Instance<PublicKeyProvider> publicKeyProviders) {
        // If X-App is not specified, assume fabric8-launcher
        final String app = Objects.toString(request.getHeader(HEADER), DEFAULT_APP).toUpperCase();
        final Application.ApplicationType type;
        try {
            type = valueOf(app);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Header 'X-App' has an invalid value: " + app);
        }
        Instance<PublicKeyProvider> publicKeyProvider = publicKeyProviders.select(PublicKeyProvider.class, of(type));
        if (publicKeyProvider.isUnsatisfied()) {
            throw new IllegalArgumentException("Identity provider not found for " + app);
        }
        return publicKeyProvider.get();
    }
}