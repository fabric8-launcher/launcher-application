package io.fabric8.launcher.osio.web.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.client.api.OsioWitClient;
import io.fabric8.launcher.osio.client.api.Tenant;
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
    public OpenShiftService createOpenShiftService(@Application(OSIO) final IdentityProvider identityProvider) {
        Identity identity = identityProvider.getIdentity(IdentityProvider.ServiceType.OPENSHIFT)
                .orElseThrow(() -> new IllegalStateException("Invalid OSIO token"));
        return openShiftServiceFactory.create(getOpenShiftCluster(), identity);
    }

    @Produces
    @RequestScoped
    public Tenant produceTenant(final OsioWitClient witClient) {
        return witClient.getTenant();
    }

}
