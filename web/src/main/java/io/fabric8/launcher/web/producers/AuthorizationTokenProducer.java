package io.fabric8.launcher.web.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

import io.fabric8.launcher.base.identity.TokenIdentity;

import static io.fabric8.launcher.base.identity.TokenIdentity.fromBearerAuthorizationHeader;
import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class AuthorizationTokenProducer {


    @Produces
    @RequestScoped
    TokenIdentity getIdentityProvider(HttpServletRequest request) {
        requireNonNull(request, "request must be specified.");
        return fromBearerAuthorizationHeader(request.getHeader(AUTHORIZATION));
    }
}