package io.fabric8.launcher.base.identity;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAPublicKeyConverter {

    public static final String PUBLIC_KEY_ALGORITHM = "RSA";

    private static final KeyFactory kf;

    static {
        try {
            kf = KeyFactory.getInstance(PUBLIC_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Something went terribly wrong here, cannot create key factory for " + PUBLIC_KEY_ALGORITHM, e);
        }
    }

    private RSAPublicKeyConverter() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Takes RSA key values of Json Web Key (see https://tools.ietf.org/html/rfc7517) and converts it to instance of
     * RSAPublicKey.
     *
     * @param mod defined as "n" key in JWK in Base64 (ignoring illegal chars)
     * @param exp defined as "e" key in JWK in Base64
     * @return RSAPublicKey instance
     *
     * @throws IllegalStateException if passed values do not conform key specification to produce a public key
     */
    public static RSAPublicKey fromJWK(String mod, String exp) {
        try {
            final BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(stripHeaderAndFooter(mod)));
            final BigInteger exponent = new BigInteger(1, Base64.getDecoder().decode(stripHeaderAndFooter(exp)));
            return (RSAPublicKey) kf.generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (InvalidKeySpecException ex) {
            throw new IllegalStateException("Failed converting JWK to RSA", ex);
        }
    }

    /**
     * Converts string-based RSA key in X509 spec to RSAPublicKey
     *
     * @param rsaKey string value of  RSA key in X509
     * @return converted instance of {@link RSAPublicKey}
     *
     * @throws IllegalStateException if passed values do not conform key specification to produce a public key
     */
    public static RSAPublicKey fromString(String rsaKey) {
        try {
            final X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(stripHeaderAndFooter(rsaKey)));
            return (RSAPublicKey) kf.generatePublic(keySpecX509);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Unable to create RSA Public Key", e);
        }
    }

    private static String stripHeaderAndFooter(String key) {
        return key.replaceAll("\\n", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "");
    }
}
