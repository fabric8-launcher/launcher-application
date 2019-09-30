package io.fabric8.launcher.core.impl;

import io.fabric8.launcher.base.EnvironmentEnum;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public enum CoreEnvironment implements EnvironmentEnum {
    LAUNCHER_PREFETCH_BOOSTERS,
    LAUNCHER_BOOSTER_CATALOG_FILTER,
    LAUNCHER_KEYCLOAK_URL,
    LAUNCHER_KEYCLOAK_REALM,
    HOSTNAME,
    LAUNCHER_FILTER_RUNTIME,
    LAUNCHER_FILTER_VERSION
}
