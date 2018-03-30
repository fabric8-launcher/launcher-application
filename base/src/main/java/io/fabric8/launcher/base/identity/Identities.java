package io.fabric8.launcher.base.identity;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Helper class for Identity
 */
final class Identities {

    private static final Predicate<String> BEARER_AUTHENTICATION_MATCHER = Pattern.compile("^Bearer \\S+$").asPredicate();
    private static final Predicate<String> TOKEN_PREDICATE = Pattern.compile("^\\S+$").asPredicate();

    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";

    private static final String PRIVATE_TOKEN_HEADER_KEY = "Private-Token";

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String BASIC_PREFIX = "Basic ";

    private Identities() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Create an header authorization key for a specific identity in order to prepare a request.
     *
     * ex: "Authorization", "Private-Token", ...
     *
     * @param identity the {@link Identity}
     * @return the header key
     */
    static String createRequestAuthorizationHeaderKey(final Identity identity) {
        final AtomicReference<String> keyRef = new AtomicReference<>();
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(final TokenIdentity token) {
                keyRef.set(getTokenIdentityHeaderKey(token));
            }

            @Override
            public void visit(final UserPasswordIdentity userPassword) {
                keyRef.set(AUTHORIZATION_HEADER_KEY);
            }
        });
        if (keyRef.get() == null) {
            throw new IllegalStateException("this IdentityVisitor should implement all kind of identities.");
        }
        return keyRef.get();
    }

    /**
     * Create an header authorization value for a specific identity in order to prepare a request.
     *
     * ex: "Bearer <token>", "Basic <Base64Auth>"
     *
     * @param identity the {@link Identity}
     * @return the header value
     */
    static String createRequestAuthorizationHeaderValue(final Identity identity) {
        final AtomicReference<String> valueRef = new AtomicReference<>();
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(final TokenIdentity token) {
                valueRef.set(getTokenIdentityHeaderValue(token));
            }

            @Override
            public void visit(final UserPasswordIdentity userPassword) {
                valueRef.set(createBasicAuthentication(userPassword.getUsername(), userPassword.getPassword()));
            }
        });
        if (valueRef.get() == null) {
            throw new IllegalStateException("this IdentityVisitor should implement all kind of identities.");
        }
        return valueRef.get();
    }

    /**
     * Removes the "Bearer " prefix in this token if it exists
     *
     * @param bearerAuthenticationHeader
     * @return
     */
    static String removeBearerPrefix(String bearerAuthenticationHeader) {
        requireNonNull(bearerAuthenticationHeader, "bearerAuthenticationHeader must be specified.");
        if (!isBearerAuthentication(bearerAuthenticationHeader)) {
            throw new IllegalArgumentException("Invalid bearer authentication header.");
        }
        return bearerAuthenticationHeader.substring(BEARER_PREFIX.length());
    }

    static String addBearerPrefix(String token) {
        requireNonNull(token, "token must be specified.");
        return BEARER_PREFIX + token;
    }

    static boolean isBearerAuthentication(final String authenticationHeader) {
        return authenticationHeader != null && BEARER_AUTHENTICATION_MATCHER.test(authenticationHeader);
    }

    static boolean isTokenOnly(final String token) {
        return token != null && TOKEN_PREDICATE.test(token);
    }

    private static String getTokenIdentityHeaderValue(final TokenIdentity identity) {
        switch (identity.getType()) {
            case AUTHORIZATION:
                return addBearerPrefix(identity.getToken());
            case PRIVATE_TOKEN:
                return identity.getToken();
            default:
                throw new IllegalStateException("This identity type is not implemented: " + identity.getType());
        }
    }

    private static String getTokenIdentityHeaderKey(final TokenIdentity identity) {
        switch (identity.getType()) {
            case AUTHORIZATION:
                return AUTHORIZATION_HEADER_KEY;
            case PRIVATE_TOKEN:
                return PRIVATE_TOKEN_HEADER_KEY;
            default:
                throw new IllegalStateException("This identity type is not implemented: " + identity.getType());
        }
    }

    private static String createBasicAuthentication(final String userName, final String password) {
        final String authorization = userName + ":" + password;
        String encodedAuthorization = Base64.getEncoder().encodeToString(authorization.getBytes());
        return BASIC_PREFIX + encodedAuthorization;
    }
}
