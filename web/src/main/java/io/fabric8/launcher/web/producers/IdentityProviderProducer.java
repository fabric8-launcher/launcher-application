package io.fabric8.launcher.web.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.core.spi.IdentityProvider;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class IdentityProviderProducer extends ApplicationTypeBasedProducer<IdentityProvider> {


    @Produces
    @RequestScoped
    IdentityProvider getIdentityProvider(HttpServletRequest request, @Any Instance<IdentityProvider> identities) {
        return extractAppBasedImplementation(request, identities, IdentityProvider.class);
    }
}