package io.fabric8.launcher.osio.web.producers;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;
import static io.fabric8.launcher.osio.OsioConfigs.getOpenShiftCluster;

@RequestScoped
public final class OpenShiftServiceProducer {

    @Produces
    @RequestScoped
    @Application(OSIO)
    public OpenShiftService createOpenShiftService(OpenShiftServiceFactory openShiftServiceFactory, @Application(OSIO) final IdentityProvider identityProvider, final TokenIdentity authorization) {
        Identity identity = identityProvider.getIdentity(authorization, IdentityProvider.ServiceType.OPENSHIFT)
                .orElseThrow(() -> new IllegalStateException("Invalid OSIO token"));
        return openShiftServiceFactory.create(getOpenShiftCluster(), identity);
    }


}