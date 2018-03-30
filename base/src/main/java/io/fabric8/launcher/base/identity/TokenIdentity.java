package io.fabric8.launcher.base.identity;

import org.immutables.value.Value;

import static io.fabric8.launcher.base.identity.Identities.isBearerAuthentication;
import static io.fabric8.launcher.base.identity.Identities.isTokenOnly;
import static io.fabric8.launcher.base.identity.Identities.removeBearerPrefix;
import static io.fabric8.launcher.base.identity.TokenIdentity.Type.AUTHORIZATION;
import static java.util.Objects.requireNonNull;

@Value.Immutable
public interface TokenIdentity extends Identity {

    @Value.Default
    default Type getType() {
        return AUTHORIZATION;
    }

    String getToken();

    @Override
    default void accept(IdentityVisitor visitor) {
        visitor.visit(this);
    }

    static TokenIdentity fromBearerAuthorizationHeader(String authorizationHeader) {
        requireNonNull(authorizationHeader, "authorizationHeader must be specified.");
        if (!isBearerAuthentication(authorizationHeader)) {
            throw new IllegalArgumentException("Invalid bearer authentication header: " + authorizationHeader);
        }
        return ImmutableTokenIdentity.builder()
                .token(removeBearerPrefix(authorizationHeader))
                .build();
    }

    static TokenIdentity of(String token) {
        requireNonNull(token, "token must be specified.");
        if (!isTokenOnly(token)) {
            throw new IllegalArgumentException("Invalid token: " + token);
        }
        return ImmutableTokenIdentity.builder()
                .token(token)
                .build();
    }

    static TokenIdentity of(Type type, String token) {
        return ImmutableTokenIdentity.builder()
                .type(type)
                .token(token)
                .build();
    }

    enum Type {
        AUTHORIZATION, PRIVATE_TOKEN
    }

}
