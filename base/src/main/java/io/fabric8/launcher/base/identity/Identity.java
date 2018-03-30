package io.fabric8.launcher.base.identity;

import java.util.function.Consumer;

import org.immutables.value.Value;

import static io.fabric8.launcher.base.identity.Identities.createRequestAuthorizationHeaderKey;
import static io.fabric8.launcher.base.identity.Identities.createRequestAuthorizationHeaderValue;

/**
 * Represents an identity used by authentication engines.
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface Identity extends Consumer<IdentityVisitor> {

    @Value.Lazy
    default String toRequestHeaderKey() {
        return createRequestAuthorizationHeaderKey(this);
    }

    @Value.Lazy
    default String toRequestHeaderValue() {
        return createRequestAuthorizationHeaderValue(this);
    }

}