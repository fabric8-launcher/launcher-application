package io.fabric8.launcher.osio.producers;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;
import static io.fabric8.launcher.osio.producers.OsioOpenShifts.OSIO_CLUSTER;

@RequestScoped
public class OpenShiftServiceProducer {

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;


    @Produces
    @Application(OSIO)
    public OpenShiftService createOpenShiftService(HttpServletRequest request, IdentityProvider identityProvider) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        Identity identity = identityProvider.getIdentity(IdentityProvider.ServiceType.OPENSHIFT, authorization)
                .orElseThrow(() -> new IllegalStateException("Invalid OSIO token"));
        return openShiftServiceFactory.create(OSIO_CLUSTER, identity);
    }
}
