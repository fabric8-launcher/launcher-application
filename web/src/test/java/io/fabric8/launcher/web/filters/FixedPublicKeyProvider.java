package io.fabric8.launcher.web.filters;

import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import io.fabric8.launcher.base.identity.RSAPublicKeyConverter;
import io.fabric8.launcher.core.spi.PublicKeyProvider;

import static io.fabric8.launcher.base.test.identity.TokenFixtures.PUBLIC_KEY;

/**
 * Mock PublicKeyProvider. See https://quarkus.io/guides/getting-started-testing
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class FixedPublicKeyProvider implements PublicKeyProvider {
    @Override
    public Optional<RSAPublicKey> getKey(String keyId) {
        return Optional.of(RSAPublicKeyConverter.fromString(PUBLIC_KEY));
    }

}
