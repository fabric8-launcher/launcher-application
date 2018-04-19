package io.fabric8.launcher.core.spi;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.ImmutableTokenIdentity;
import io.fabric8.launcher.base.identity.TokenIdentity;

import static java.util.stream.Collectors.toMap;

/**
 * An utility class for {@link IdentityProviders}.
 */
public final class IdentityProviders {

    private IdentityProviders() {
        throw new IllegalAccessError("utility class");
    }

    public static Map<String, Optional<Identity>> getIdentities(final IdentityProvider identityProvider, final TokenIdentity authorization, final Set<String> services) {
        final TokenIdentity immutableAuthorization = ImmutableTokenIdentity.copyOf(authorization);
        final Map<String, CompletableFuture<Optional<Identity>>> futureMap = services.stream()
                .collect(toMap(Function.identity(), s -> identityProvider.getIdentityAsync(immutableAuthorization, s)));
        final Collection<CompletableFuture<Optional<Identity>>> futures = futureMap.values();
        final CompletableFuture<Map<String, Optional<Identity>>> mapFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> joinMap(futureMap));
        try {
            return mapFuture.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while getting identitoes for service", e);
        } catch (final ExecutionException e) {
            throw new IllegalStateException("Error while getting identities", e);
        }

    }

    private static Map<String, Optional<Identity>> joinMap(final Map<String, CompletableFuture<Optional<Identity>>> futureMap) {
        return futureMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().join()));
    }

}
