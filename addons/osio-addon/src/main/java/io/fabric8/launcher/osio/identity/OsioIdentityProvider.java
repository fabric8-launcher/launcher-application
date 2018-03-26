package io.fabric8.launcher.osio.identity;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.client.OsioApiClient;
import io.fabric8.launcher.osio.client.OsioApiClientImpl;

import static io.fabric8.launcher.base.identity.IdentityFactory.createFromToken;
import static io.fabric8.launcher.base.identity.IdentityHelper.removeBearerPrefix;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;
import static io.fabric8.launcher.osio.OsioConfigs.ExternalServices.getGithubServiceName;


@ApplicationScoped
@Application(OSIO)
public class OsioIdentityProvider implements IdentityProvider {


    @Override
    public Optional<Identity> getIdentity(final String service, final String authorizationHeader) {
        final TokenIdentity osioIdentity = createFromToken(removeBearerPrefix(authorizationHeader));
        final OsioApiClient osioApiClient = new OsioApiClientImpl(osioIdentity);
        switch (service) {
            case IdentityProvider.ServiceType.GITHUB:
                return osioApiClient.getTokenForService(getGithubServiceName())
                        .map(IdentityFactory::createFromToken);
            case IdentityProvider.ServiceType.OPENSHIFT:
                return Optional.of(osioIdentity);
            default:
                return osioApiClient.getTokenForService(service)
                        .map(IdentityFactory::createFromToken);
        }
    }
}
