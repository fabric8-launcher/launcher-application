package io.fabric8.launcher.base.identity;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

public class RSAPublicKeyConverter {

    public static final String PUBLIC_KEY_ALGORITHM = "RSA";

    private static final KeyFactory kf;

    static {
        try {
            kf = KeyFactory.getInstance(PUBLIC_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private RSAPublicKeyConverter() {
        throw new IllegalAccessError("Utility class");
    }

    public static RSAPublicKey fromJWT(String mod, String exp) {
        try {
            final BigInteger modulus = new BigInteger(1, Base64.decodeBase64(mod));
            final BigInteger exponent = new BigInteger(1, Base64.decodeBase64(exp));
            return (RSAPublicKey) kf.generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (InvalidKeySpecException ex) {
            throw new IllegalStateException("Failed converting JWK to RSA", ex);
        }
    }

    public static RSAPublicKey fromString(String key) {
        try {
            key = stripHeaderAndFooter(key);
            final X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(java.util.Base64.getDecoder().decode(key));
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
