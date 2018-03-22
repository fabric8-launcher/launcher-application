package io.fabric8.launcher.osio.identity;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;

import static io.fabric8.launcher.base.identity.IdentityFactory.createFromToken;
import static io.fabric8.launcher.base.identity.IdentityHelper.removeBearerPrefix;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;
import static io.fabric8.launcher.osio.EnvironmentVariables.ExternalServices.getGithubServiceName;
import static io.fabric8.launcher.osio.identity.OsioIdentityRequests.getServiceToken;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Application(OSIO)
@ApplicationScoped
public class OsioIdentityProvider implements IdentityProvider {

    @Override
    public Optional<Identity> getIdentity(String service, String authorization) {
        final TokenIdentity osioIdentity = createFromToken(removeBearerPrefix(authorization));
        switch (service) {
            case ServiceType.GITHUB:
                return getServiceToken(osioIdentity, getGithubServiceName())
                        .map(IdentityFactory::createFromToken);
            case ServiceType.OPENSHIFT:
                return Optional.of(osioIdentity);
            default:
                return getServiceToken(osioIdentity, service)
                        .map(IdentityFactory::createFromToken);
        }
    }
}
