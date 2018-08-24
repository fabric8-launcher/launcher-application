package io.fabric8.launcher.core.impl.identity;

import org.immutables.value.Value;

import static io.fabric8.launcher.core.impl.CoreEnvVarSysPropNames.LAUNCHER_KEYCLOAK_REALM;
import static io.fabric8.launcher.core.impl.CoreEnvVarSysPropNames.LAUNCHER_KEYCLOAK_URL;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
public interface KeycloakParameters {

    String TOKEN_URL_TEMPLATE = "%s/realms/%s/broker/%s/token";

    @Value.Default
    default String getUrl() {
        return LAUNCHER_KEYCLOAK_URL.valueRequired();
    }

    @Value.Default
    default String getRealm() {
        return LAUNCHER_KEYCLOAK_REALM.valueRequired();
    }

    @Value.Derived
    default String buildUrl(String service) {
        return String.format(TOKEN_URL_TEMPLATE, getUrl(), getRealm(), service);
    }
}