package io.fabric8.launcher.osio.client;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.TokenIdentity;

final class OsioTests {

    static final String LAUNCHER_OSIO_TOKEN = "LAUNCHER_OSIO_TOKEN";

    static TokenIdentity getTestAuthorization() {
        return TokenIdentity.of(EnvironmentSupport.getRequiredEnvVarOrSysProp(LAUNCHER_OSIO_TOKEN));
    }

}
