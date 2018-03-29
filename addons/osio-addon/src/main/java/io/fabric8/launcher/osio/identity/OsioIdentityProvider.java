package io.fabric8.launcher.osio.identity;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.client.api.OsioAuthClient;
import io.fabric8.launcher.osio.client.api.Tenant;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;


@Dependent
@Application(OSIO)
public class OsioIdentityProvider implements IdentityProvider {

    private static final String GITHUB_SERVICENAME = "https://github.com";

    @Inject
    private OsioAuthClient authClient;

    @Inject
    private Tenant tenant;

    @Override
    public Optional<Identity> getIdentity(final String service) {
        switch (service) {
            case IdentityProvider.ServiceType.GITHUB:
                return authClient.getTokenForService(GITHUB_SERVICENAME)
                        .map(IdentityFactory::createFromToken);
            case IdentityProvider.ServiceType.OPENSHIFT:
                return Optional.ofNullable(tenant.getIdentity());
            default:
                return authClient.getTokenForService(service)
                        .map(IdentityFactory::createFromToken);
        }
    }
}
