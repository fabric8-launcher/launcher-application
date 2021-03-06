package io.fabric8.launcher.core.spi;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.fabric8.launcher.base.identity.Identity;

/**
 * An {@link IdentityProvider} returns identities for any given service
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface IdentityProvider {

    IdentityProvider NULL_PROVIDER = new IdentityProvider() {
        @Override
        public CompletableFuture<Optional<Identity>> getIdentityAsync(Identity authorization, String service) {
            return CompletableFuture.completedFuture(Optional.ofNullable(authorization));
        }

        @Override
        public Optional<Identity> getIdentity(Identity authorization, String service) {
            return Optional.ofNullable(authorization);
        }
    };

    /**
     * This method is executed asynchronously, therefore a {@link CompletableFuture} is returned
     *
     * @param authorization the {@link Identity} used in the Authorization header
     * @param service       the service where this identity belongs to. See {@link ServiceType}
     * @return a {@link CompletableFuture} returning an {@link Optional<Identity>}
     */
    CompletableFuture<Optional<Identity>> getIdentityAsync(Identity authorization, String service);

    /**
     * Return the identity for a given authorization and service.
     * <p>
     * The default implementation invokes getIdentityAsync for implementation simplification purposes
     *
     * @param authorization the {@link Identity} used in the Authorization header
     * @param service       the service where this identity belongs to. See {@link ServiceType}
     * @return a {@link CompletableFuture} returning an {@link Optional<Identity>}
     */
    Optional<Identity> getIdentity(Identity authorization, String service);

    interface ServiceType {
        String OPENSHIFT = "openshift-v4";
    }
}
