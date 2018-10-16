package io.fabric8.launcher.core.impl.identity;

import org.immutables.value.Value;

import static io.fabric8.launcher.core.impl.CoreEnvVarSysPropNames.LAUNCHER_KEYCLOAK_REALM;
import static io.fabric8.launcher.core.impl.CoreEnvVarSysPropNames.LAUNCHER_KEYCLOAK_URL;
import static java.lang.String.format;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
public interface KeycloakParameters {

    String TOKEN_URL_TEMPLATE = "%s/realms/%s/broker/%s/token";

    // As of version 3.2 this is the resource exposing JWK (undocumented)
    // When sso upgrades to 4.x we can try to switch to https://www.keycloak.org/docs-api/4.5/rest-api/index.html#_key_resource
    String KEY_URL_TEMPLATE = "%s/realms/%s/protocol/openid-connect/certs";

    @Value.Default
    default String getUrl() {
        return LAUNCHER_KEYCLOAK_URL.valueRequired();
    }

    @Value.Default
    default String getRealm() {
        return LAUNCHER_KEYCLOAK_REALM.valueRequired();
    }

    @Value.Derived
    default String buildTokenUrl(String service) {
        return format(TOKEN_URL_TEMPLATE, getUrl(), getRealm(), service);
    }

    @Value.Derived
    default String buildKeysUrl() {
        return format(KEY_URL_TEMPLATE, getUrl(), getRealm());
    }
}