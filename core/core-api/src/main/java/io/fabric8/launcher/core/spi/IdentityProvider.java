package io.fabric8.launcher.core.spi;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;

/**
 * An {@link IdentityProvider} returns identities for any given service
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface IdentityProvider {

    /**
     * This method is executed asynchronously, therefore a {@link CompletableFuture} is returned
     *
     * @param authorization the {@link TokenIdentity} used in the Authorization header
     * @param service       the service where this identity belongs to. See {@link ServiceType}
     * @return a {@link CompletableFuture} returning an {@link Optional<Identity>}
     */
    CompletableFuture<Optional<Identity>> getIdentityAsync(TokenIdentity authorization, String service);

    /**
     * Return the identity for a given authorization and service.
     *
     * The default implementation invokes getIdentityAsync for implementation simplification purposes
     *
     * @param authorization the {@link TokenIdentity} used in the Authorization header
     * @param service       the service where this identity belongs to. See {@link ServiceType}
     * @return a {@link CompletableFuture} returning an {@link Optional<Identity>}
     */
    Optional<Identity> getIdentity(TokenIdentity authorization, String service);

    interface ServiceType {
        String GITHUB = "github";
        String OPENSHIFT = "openshift-v3";
    }
}
