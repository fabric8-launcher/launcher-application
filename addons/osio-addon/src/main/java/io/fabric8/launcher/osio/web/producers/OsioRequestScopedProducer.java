package io.fabric8.launcher.osio.web.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.client.OsioApiClient;
import io.fabric8.launcher.osio.client.OsioApiClientImpl;
import io.fabric8.launcher.osio.client.Tenant;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

import static io.fabric8.launcher.base.identity.IdentityFactory.createFromToken;
import static io.fabric8.launcher.base.identity.IdentityHelper.removeBearerPrefix;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;
import static io.fabric8.launcher.osio.OsioConfigs.getOpenShiftCluster;


@ApplicationScoped
public final class OsioRequestScopedProducer {

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Produces
    @RequestScoped
    @Application(OSIO)
    public OpenShiftService createOpenShiftService(final IdentityProvider identityProvider) {
        Identity identity = identityProvider.getIdentity(IdentityProvider.ServiceType.OPENSHIFT)
                .orElseThrow(() -> new IllegalStateException("Invalid OSIO token"));
        return openShiftServiceFactory.create(getOpenShiftCluster(), identity);
    }

    @Produces
    @RequestScoped
    public OsioApiClient createOsioApiClient(final HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final TokenIdentity osioIdentity = createFromToken(removeBearerPrefix(authorizationHeader));
        return new OsioApiClientImpl(osioIdentity);
    }

    @Produces
    @RequestScoped
    public Tenant produceTenant(final OsioApiClient osioApiClient) {
        return osioApiClient.getTenant();
    }

}
