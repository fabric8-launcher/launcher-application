package io.fabric8.launcher.web.endpoints.osio;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.osio.client.OsioWitClient;

final class OsioTests {

    static final String LAUNCHER_OSIO_TOKEN = "LAUNCHER_OSIO_TOKEN";

    static TokenIdentity getTestAuthorization() {
        return TokenIdentity.of(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_OSIO_TOKEN));
    }

    static TokenIdentity getOsioIdentity() {
        return TokenIdentity.of(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_OSIO_TOKEN));
    }

    static OsioWitClient getWitClient() {
        return new OsioWitClient(OsioTests.getTestAuthorization(), new HttpClient());
    }

}
