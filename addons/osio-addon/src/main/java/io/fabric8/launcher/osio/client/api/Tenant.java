package io.fabric8.launcher.osio.client.api;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    UserInfo getUserInfo();

    /**
     * @return The Openshift.io token used to authenticate this user in auth.openshift.io
     */
    @JsonIgnore
    @Nullable
    TokenIdentity getIdentity();

    List<Namespace> getNamespaces();

    @JsonIgnore
    @Value.Derived
    default Namespace getDefaultUserNamespace() {
        return getNamespaces().stream()
                .filter(Namespace::isUserNamespace)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No user namespace found for " + getUserInfo().getUsername()));
    }

    @Value.Immutable
    @JsonDeserialize(as = ImmutableNamespace.class)
    @JsonSerialize(as = ImmutableNamespace.class)
    interface Namespace {

        String getName();

        String getType();

        String getClusterUrl();

        String getClusterConsoleUrl();

        /**
         * Returns true if this namespace is a user namespace
         */
        @Value.Derived
        @JsonIgnore
        default boolean isUserNamespace() {
            return Objects.equals("user", getType());
        }
    }

    @Value.Immutable
    @JsonDeserialize(as = ImmutableUserInfo.class)
    @JsonSerialize(as = ImmutableUserInfo.class)
    interface UserInfo {
        String getUsername();
        String getEmail();
    }

}
