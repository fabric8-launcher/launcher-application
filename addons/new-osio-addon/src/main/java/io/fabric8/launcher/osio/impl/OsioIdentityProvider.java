package io.fabric8.launcher.osio.impl;

import java.util.Optional;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Application("osio")
public class OsioIdentityProvider implements IdentityProvider {

    @Override
    public Optional<Identity> getIdentity(String service, String authorization) {
        switch (service) {
            case ServiceType.GITHUB:
                // TODO: GET_GITHUB_TOKEN("GitHub", URLUtils.pathJoin(EnvironmentVariables.getAuthApiURL(), "/api/token?for=https://github.com"));
                return Optional.empty();
            case ServiceType.OPENSHIFT:
                return Optional.of(IdentityFactory.createFromToken(authorization));
            default:
                return Optional.empty();
        }
    }
}
