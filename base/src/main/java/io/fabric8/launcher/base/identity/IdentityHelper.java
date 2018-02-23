package io.fabric8.launcher.base.identity;

import javax.annotation.Nullable;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Helper class for Identity
 */
public final class IdentityHelper {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BASIC_PREFIX = "Basic ";

    private IdentityHelper(){
        throw new IllegalAccessError();
    }

    /**
     * Create an header authorization key for a specific identity in order to prepare a request.
     *
     * ex: "Authorization", "Private-Token", ...
     *
     * @param identity the {@link Identity}
     * @return the header key
     */
    public static String createRequestAuthorizationHeaderKey(final Identity identity) {
        final AtomicReference<String> keyRef = new AtomicReference<>();
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(final TokenIdentity token) {
                final String key = getTokenIdentityHeaderKey(token);
                keyRef.set(key);
            }

            @Override
            public void visit(final UserPasswordIdentity userPassword) {
                keyRef.set(AUTHORIZATION_HEADER);
            }
        });
        assert keyRef.get() != null : "this IdentityVisitor should implement all kind of identities.";
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
    public static String createRequestAuthorizationHeaderValue(final Identity identity) {
        final AtomicReference<String> valueRef = new AtomicReference<>();
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(final TokenIdentity token) {
                final String key = getTokenIdentityHeaderKey(token);
                final String value = AUTHORIZATION_HEADER.equalsIgnoreCase(key) ?
                        createBearerAuthentication(token.getToken()) : token.getToken();
                valueRef.set(value);
            }

            @Override
            public void visit(final UserPasswordIdentity userPassword) {
                final UserPasswordIdentity userPasswordIdentity = (UserPasswordIdentity) identity;
                final String value = createBasicAuthentication(userPasswordIdentity.getUsername(), userPasswordIdentity.getPassword());
                valueRef.set(value);
            }
        });
        assert valueRef.get() != null : "this IdentityVisitor should implement all kind of identities.";
        return valueRef.get();
    }

    /**
     * Removes the "Bearer " prefix in this token if it exists
     *
     * @param token
     * @return
     */
    @Nullable
    public static String removeBearerPrefix(String token) {
        if (token == null)
            return null;
        if (token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length());
        }
        return token;
    }

    private static String getTokenIdentityHeaderKey(final TokenIdentity identity) {
        return identity.getType().orElse(AUTHORIZATION_HEADER);
    }

    private static String createBearerAuthentication(final String token) {
        return (token.startsWith(BEARER_PREFIX)) ? token : BEARER_PREFIX + token;
    }

    private static String createBasicAuthentication(final String userName, final String password) {
        final String authorization = userName + ":" + password;
        String encodedAuthorization = Base64.getEncoder().encodeToString(authorization.getBytes());
        return BASIC_PREFIX + encodedAuthorization;
    }
}
