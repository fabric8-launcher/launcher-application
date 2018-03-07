package io.fabric8.launcher.osio.tenants;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.osio.tenant.ImmutableTenant;
import io.fabric8.launcher.osio.tenant.Tenant;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/osio/tenant")
@RequestScoped
public class TenantTestEndpoint {

    @Inject
    private Tenant tenant;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Tenant getTenant() {
        return ImmutableTenant.copyOf(tenant);

    }
}
