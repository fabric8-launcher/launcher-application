package io.fabric8.launcher.web.endpoints.osio;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.osio.client.OsioWitClient;
import io.fabric8.launcher.osio.client.Space;
import io.fabric8.launcher.osio.client.Tenant;

final class OsioTests {

    static final String LAUNCHER_OSIO_TOKEN = "LAUNCHER_OSIO_TOKEN";

    static final String LAUNCHER_OSIO_SPACE = "LAUNCHER_OSIO_SPACE";

    static TokenIdentity getTestAuthorization() {
        return TokenIdentity.of(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_OSIO_TOKEN));
    }

    static TokenIdentity getOsioIdentity() {
        return TokenIdentity.of(EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_OSIO_TOKEN));
    }

    static OsioWitClient getWitClient() {
        return new OsioWitClient(OsioTests.getTestAuthorization());
    }

    static Tenant getTenant() {
        return getWitClient().getTenant();
    }

    static Space getOsioSpace() {
        String spaceName = EnvironmentSupport.INSTANCE.getRequiredEnvVarOrSysProp(LAUNCHER_OSIO_SPACE);
        return getWitClient().findSpaceByName(getTenant().getUserInfo().getUsername(), spaceName);
    }
}
