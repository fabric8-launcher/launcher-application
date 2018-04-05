package io.fabric8.launcher.base.identity;

import java.util.function.Consumer;

import io.fabric8.launcher.base.http.AuthorizationType;
import org.immutables.value.Value;

import static io.fabric8.launcher.base.http.Authorizations.createAuthorization;

/**
 * Represents an identity used by authentication engines.
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface Identity extends Consumer<IdentityVisitor> {

    /**
     * Get the default authorization type to use with this identity
     *
     * @return the {@link AuthorizationType}
     */
    AuthorizationType getDefaultAuthorizationType();

    /**
     * Get the default request authorization for this identity
     *
     * @return the request authorization
     */
    @Value.Lazy
    default String toRequestAuthorization() {
        return createAuthorization(this, getDefaultAuthorizationType());
    }

    /**
     * Get the request authorization for this identity using the specified authorization type
     *
     * @param type the {@link AuthorizationType}
     * @return the request authorization
     */
    @Value.Derived
    default String toRequestAuthorization(AuthorizationType type) {
        return createAuthorization(this, type);
    }
}