package io.fabric8.launcher.osio.tenant;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.identity.TokenIdentity;

import static io.fabric8.launcher.base.identity.IdentityFactory.createFromToken;
import static io.fabric8.launcher.base.identity.IdentityHelper.removeBearerPrefix;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class TenantProducer {

    @Produces
    @RequestScoped
    public Tenant produceTenant(HttpServletRequest servletRequest) {
        final String authorizationHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        TokenIdentity osioToken = createFromToken(removeBearerPrefix(authorizationHeader));
        return TenantRequests.getTenant(osioToken);
    }

}
