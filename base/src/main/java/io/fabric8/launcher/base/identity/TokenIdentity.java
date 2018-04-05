package io.fabric8.launcher.base.identity;

import io.fabric8.launcher.base.http.AuthorizationType;
import org.immutables.value.Value;

import static io.fabric8.launcher.base.http.Authorizations.isBearerAuthentication;
import static io.fabric8.launcher.base.http.Authorizations.isTokenOnly;
import static io.fabric8.launcher.base.http.Authorizations.removeBearerPrefix;
import static java.util.Objects.requireNonNull;

@Value.Immutable
public interface TokenIdentity extends Identity {

    @Override
    default AuthorizationType getDefaultAuthorizationType() {
        return AuthorizationType.BEARER_TOKEN;
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

}
