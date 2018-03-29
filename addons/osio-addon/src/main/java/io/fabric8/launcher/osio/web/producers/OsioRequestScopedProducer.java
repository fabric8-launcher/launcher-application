package io.fabric8.launcher.osio.web.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.client.api.OsioAuthClient;
import io.fabric8.launcher.osio.client.api.OsioJenkinsClient;
import io.fabric8.launcher.osio.client.api.OsioWitClient;
import io.fabric8.launcher.osio.client.api.Tenant;
import io.fabric8.launcher.osio.client.impl.OsioAuthClientImpl;
import io.fabric8.launcher.osio.client.impl.OsioJenkinsClientImpl;
import io.fabric8.launcher.osio.client.impl.OsioWitClientImpl;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

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
    public OsioAuthClient createOsioAuthClient(final HttpServletRequest request) {
        return new OsioAuthClientImpl(request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Produces
    @RequestScoped
    public OsioWitClient createOsioWitClient(final HttpServletRequest request) {
        return new OsioWitClientImpl(request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Produces
    @RequestScoped
    public OsioJenkinsClient createOsioJenkinsClient(final HttpServletRequest request,
                                                     final IdentityProvider identityProvider) {
        return new OsioJenkinsClientImpl(request.getHeader(HttpHeaders.AUTHORIZATION), identityProvider);
    }

    @Produces
    @RequestScoped
    public Tenant produceTenant(final OsioWitClient witClient) {
        return witClient.getTenant();
    }

}
