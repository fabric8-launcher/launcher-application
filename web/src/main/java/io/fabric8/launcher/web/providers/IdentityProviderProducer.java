package io.fabric8.launcher.web.providers;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;

import static io.fabric8.launcher.core.spi.Application.ApplicationLiteral.of;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class IdentityProviderProducer {

    @Produces
    @RequestScoped
    IdentityProvider getIdentityProvider(HttpServletRequest request, Instance<IdentityProvider> identities, Application application) {
        // Because DefaultIdentityProvider is @Default, this call will never fail
        return identities.select(application).get();
    }
}