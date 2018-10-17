package io.fabric8.launcher.core.spi;

import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

/**
 * Returns key from given identity provider recognized by kid claim coming in JWT header
 */
public interface PublicKeyProvider {

    Optional<RSAPublicKey> getKey(String keyId);

}
