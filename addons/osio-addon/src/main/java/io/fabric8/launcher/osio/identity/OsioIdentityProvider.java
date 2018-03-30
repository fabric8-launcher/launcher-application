package io.fabric8.launcher.osio.identity;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.client.api.OsioAuthClient;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;
import static java.util.Objects.requireNonNull;


@Dependent
@Application(OSIO)
public class OsioIdentityProvider implements IdentityProvider {

    private static final String GITHUB_SERVICENAME = "https://github.com";

    private final TokenIdentity authorization;
    private final OsioAuthClient authClient;

    @Inject
    public OsioIdentityProvider(final TokenIdentity authorization, final OsioAuthClient osioAuthClient) {
        this.authorization = requireNonNull(authorization, "authorization must be specified.");
        this.authClient = requireNonNull(osioAuthClient, "osioAuthClient must be specified.");
    }

    @Override
    public Optional<Identity> getIdentity(final String service) {
        switch (service) {
            case ServiceType.GITHUB:
                return authClient.getTokenForService(GITHUB_SERVICENAME)
                        .map(TokenIdentity::of);
            case ServiceType.OPENSHIFT:
                return Optional.of(authorization);
            default:
                return authClient.getTokenForService(service)
                        .map(TokenIdentity::of);
        }
    }
}
