package io.fabric8.launcher.base.identity;


import javax.annotation.Nullable;

import io.fabric8.launcher.base.http.AuthorizationType;
import org.immutables.value.Value;

@Value.Immutable
public interface UserPasswordIdentity extends Identity {

    @Value.Parameter
    String getUsername();

    @Value.Parameter
    @Nullable
    String getPassword();

    @Override
    default void accept(IdentityVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    default AuthorizationType getDefaultAuthorizationType() {
        return AuthorizationType.BASIC;
    }
}
