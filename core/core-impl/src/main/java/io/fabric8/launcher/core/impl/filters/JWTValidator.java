package io.fabric8.launcher.core.impl.filters;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import io.fabric8.launcher.core.spi.PublicKeyProvider;

/**
 * Validates JWT token by looking up public key using kid claim from the header.
 * The actual lookup strategy should be defined by {@link PublicKeyProvider} instance.
 * It assumes that RSA256 has been used as algorithm and public key is in PKCS8 format.
 */
class JWTValidator {

    private static final Logger log = Logger.getLogger(JWTValidator.class.getName());

    private final String expectedIssuer;

    private final PublicKeyProvider publicKeyProvider;

    JWTValidator(String expectedIssuer, PublicKeyProvider publicKeyProvider) {
        this.expectedIssuer = expectedIssuer;
        this.publicKeyProvider = publicKeyProvider;
    }

    boolean validate(String token) {

        final Algorithm rsa = Algorithm.RSA256(new RSAPublicKeyProvider());
        final JWTVerifier verifier = JWT.require(rsa)
                .withIssuer(expectedIssuer)
                .build();
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.log(Level.WARNING, "Could not validate token: " + e.getMessage(), e);
            return false;
        }

    }

    private class RSAPublicKeyProvider implements RSAKeyProvider {

        @Override
        public RSAPublicKey getPublicKeyById(String keyId) {
            final Optional<RSAPublicKey> key = publicKeyProvider.getKey(keyId);
            return key.orElseThrow(() -> new IllegalArgumentException("Public key not found for " + keyId));
        }

        @Override
        public RSAPrivateKey getPrivateKey() {
            throw new UnsupportedOperationException("Only public key retrieval available");
        }

        @Override
        public String getPrivateKeyId() {
            throw new UnsupportedOperationException("Only public key retrieval available");
        }
    }
}
