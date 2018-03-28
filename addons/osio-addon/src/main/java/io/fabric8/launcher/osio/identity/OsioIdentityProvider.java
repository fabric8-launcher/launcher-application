package io.fabric8.launcher.osio.identity;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.client.OsioApiClient;
import io.fabric8.launcher.osio.client.Tenant;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;
import static io.fabric8.launcher.osio.OsioConfigs.ExternalServices.getGithubServiceName;


@Dependent
@Application(OSIO)
public class OsioIdentityProvider implements IdentityProvider {

    @Inject
    private OsioApiClient osioApiClient;

    @Inject
    private Tenant tenant;

    @Override
    public Optional<Identity> getIdentity(final String service) {
        switch (service) {
            case IdentityProvider.ServiceType.GITHUB:
                return osioApiClient.getTokenForService(getGithubServiceName())
                        .map(IdentityFactory::createFromToken);
            case IdentityProvider.ServiceType.OPENSHIFT:
                return Optional.of(tenant.getIdentity());
            default:
                return osioApiClient.getTokenForService(service)
                        .map(IdentityFactory::createFromToken);
        }
    }
}
