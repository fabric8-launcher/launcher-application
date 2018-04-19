package io.fabric8.launcher.osio.identity;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.client.OsioAuthClient;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;
import static java.util.Objects.requireNonNull;


@ApplicationScoped
@Application(OSIO)
public class OsioIdentityProvider implements IdentityProvider {

    private final OsioAuthClient authClient;

    @Inject
    public OsioIdentityProvider(final OsioAuthClient authClient) {
        this.authClient = requireNonNull(authClient, "authClient must be specified");
    }

    @Override
    public CompletableFuture<Optional<Identity>> getIdentityAsync(final TokenIdentity authorization, final String service) {
        return authClient.getIdentity(authorization, service);
    }
}
