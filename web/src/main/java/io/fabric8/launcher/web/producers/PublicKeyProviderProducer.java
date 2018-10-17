package io.fabric8.launcher.web.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.core.spi.PublicKeyProvider;

@ApplicationScoped
public class PublicKeyProviderProducer extends ApplicationTypeBasedProducer<PublicKeyProvider> {

    @Produces
    @RequestScoped
    PublicKeyProvider getIdentityProvider(HttpServletRequest request, @Any Instance<PublicKeyProvider> publicKeyProviders) {
        return extractAppBasedImplementation(request, publicKeyProviders, PublicKeyProvider.class);
    }


}