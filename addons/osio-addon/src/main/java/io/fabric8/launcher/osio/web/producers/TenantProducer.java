package io.fabric8.launcher.osio.web.producers;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.osio.client.OsioWitClient;
import io.fabric8.launcher.osio.client.Tenant;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class TenantProducer {

    @Produces
    @RequestScoped
    public Tenant produceTenant(final OsioWitClient witClient) {
        return witClient.getTenant();
    }

}
