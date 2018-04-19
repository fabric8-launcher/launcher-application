package io.fabric8.launcher.core.spi;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;

/**
 * An {@link IdentityProvider} returns identities for any given service
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface IdentityProvider {

    CompletableFuture<Optional<Identity>> getIdentityAsync(TokenIdentity authorization, String service);

    default Optional<Identity> getIdentity(TokenIdentity authorization, String service) {
        try {
            return getIdentityAsync(authorization, service).get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while getting identity for service " + service, e);
        } catch (final ExecutionException e) {
            throw new IllegalStateException("Error while getting identity for service " + service, e);
        }
    }

    interface ServiceType {
        String GITHUB = "github";
        String OPENSHIFT = "openshift-v3";
    }
}
