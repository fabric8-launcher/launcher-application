package io.fabric8.launcher.core.impl.filters;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import org.jetbrains.annotations.NotNull;

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
            log.severe("Could not validate token: " + e.getMessage());
            return false;
        }

    }

    private class RSAPublicKeyProvider implements RSAKeyProvider {

        @Override
        public RSAPublicKey getPublicKeyById(String keyId) {
            final String key = publicKeyProvider.getKey(keyId);
            return fromString(key);
        }

        // Needs to be in PKCS8 format
        private RSAPublicKey fromString(String key) {
            key = stripHeaderAndFooter(key);
            try {
                final KeyFactory kf = KeyFactory.getInstance("RSA");
                final X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
                return (RSAPublicKey) kf.generatePublic(keySpecX509);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new IllegalArgumentException("Unable to create RSA Public Key", e);
            }
        }

        @NotNull
        private String stripHeaderAndFooter(String key) {
            key = key.replaceAll("\\n", "")
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "");
            return key;
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
