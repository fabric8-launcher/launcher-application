package io.fabric8.launcher.osio.tenant;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.launcher.base.identity.TokenIdentity;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableTenant.class)
@JsonSerialize(as = ImmutableTenant.class)
@Value.Style(depluralize = true)
public interface Tenant {

    String getUsername();

    String getEmail();

    /**
     * @return The Openshift.io token used to authenticate this user in auth.openshift.io
     */
    TokenIdentity getIdentity();

    List<Namespace> getNamespaces();

    @Value.Derived
    default Namespace getDefaultUserNamespace() {
        return getNamespaces().stream()
                .filter(Namespace::isUserNamespace)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No user namespace found for " + getUsername()));
    }

}
