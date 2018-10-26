package io.fabric8.launcher.core.impl.filters;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.fabric8.launcher.base.http.Authorizations;
import io.fabric8.launcher.core.api.security.Secured;

import static io.fabric8.launcher.base.http.Authorizations.isBearerAuthentication;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static javax.ws.rs.core.Response.status;

/**
 * Based on https://stackoverflow.com/questions/26777083/best-practice-for-rest-token-based-authentication-with-jax-rs-and-jersey
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecuredFilter implements ContainerRequestFilter {

    private static final Logger log = Logger.getLogger(SecuredFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) {

        // Get the Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (!isBearerAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext);
            return;
        }

        // Extract the token from the Authorization header
        String token = Authorizations.removeBearerPrefix(authorizationHeader);

        try {
            final DecodedJWT jwt = JWT.decode(token);
            propagateSecurityContext(requestContext, jwt);
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not validate token: " + e.getMessage(), e);
            abortWithUnauthorized(requestContext);
        }
    }

    private void propagateSecurityContext(ContainerRequestContext requestContext, DecodedJWT jwt) {
        final JWTSecurityContext securityContext = new JWTSecurityContext(jwt);
        requestContext.setProperty("USER_NAME", securityContext.getUserPrincipal().getName());
        requestContext.setSecurityContext(securityContext);
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(status(UNAUTHORIZED).build());
    }
}
