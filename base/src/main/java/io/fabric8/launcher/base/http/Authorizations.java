package io.fabric8.launcher.base.http;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;

import static java.util.Objects.requireNonNull;

public final class Authorizations {

    private static final Predicate<String> BEARER_AUTHENTICATION_MATCHER = Pattern.compile("^Bearer \\S+$").asPredicate();

    private static final Predicate<String> TOKEN_PREDICATE = Pattern.compile("^\\S+$").asPredicate();

    private Authorizations() {
        throw new IllegalAccessError("Utility class");
    }


    private static final String BEARER_PREFIX = "Bearer ";

    private static final String BASIC_PREFIX = "Basic ";

    /**
     * Create the authorization for the given identity and type
     *
     * @param identity the {@link Identity}
     * @param type the {@link AuthorizationType}
     * @return the authorization
     */
    public static String createAuthorization(final Identity identity, final AuthorizationType type) {
        final AtomicReference<String> authorizationRef = new AtomicReference<>();
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(final TokenIdentity token) {
                authorizationRef.set(createAuthorization(token, type));
            }

            @Override
            public void visit(final UserPasswordIdentity userPassword) {
                authorizationRef.set(createAuthorization(userPassword, type));
            }
        });
        if (authorizationRef.get() == null) {
            throw new IllegalStateException("this IdentityVisitor should implement all kind of identities.");
        }
        return authorizationRef.get();
    }

    /**
     * Removes the "Bearer " prefix in this token if it exists
     *
     * @param bearerAuthenticationHeader
     * @return
     */
    public static String removeBearerPrefix(String bearerAuthenticationHeader) {
        requireNonNull(bearerAuthenticationHeader, "bearerAuthenticationHeader must be specified.");
        if (!isBearerAuthentication(bearerAuthenticationHeader)) {
            throw new IllegalArgumentException("Invalid bearer authentication header.");
        }
        return bearerAuthenticationHeader.substring(BEARER_PREFIX.length());
    }

    public static boolean isBearerAuthentication(final String authenticationHeader) {
        return authenticationHeader != null && BEARER_AUTHENTICATION_MATCHER.test(authenticationHeader);
    }

    public static boolean isTokenOnly(final String token) {
        return token != null && TOKEN_PREDICATE.test(token);
    }

    private static String createAuthorization(final TokenIdentity identity, final AuthorizationType type) {
        if (type == AuthorizationType.BEARER_TOKEN) {
            return addBearerPrefix(identity.getToken());
        } else if (type == AuthorizationType.PRIVATE_TOKEN) {
            return identity.getToken();
        } else {
            throw new IllegalStateException("Invalid Authorization for TokenIdentity: " + type);
        }
    }

    private static String createAuthorization(final UserPasswordIdentity identity, final AuthorizationType type) {
        if (type == AuthorizationType.BASIC) {
            return createBasicAuthentication(identity.getUsername(), identity.getPassword());
        } else {
            throw new IllegalStateException("Invalid Authorization for UserPasswordIdentity :" + type);
        }
    }

    static String addBearerPrefix(String token) {
        requireNonNull(token, "token must be specified.");
        return BEARER_PREFIX + token;
    }


    private static String createBasicAuthentication(final String userName, final String password) {
        final String authorization = userName + ":" + password;
        String encodedAuthorization = Base64.getEncoder().encodeToString(authorization.getBytes());
        return BASIC_PREFIX + encodedAuthorization;
    }

}
