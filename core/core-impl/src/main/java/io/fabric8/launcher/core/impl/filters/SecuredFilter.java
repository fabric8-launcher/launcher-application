package io.fabric8.launcher.core.impl.filters;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.fabric8.launcher.base.http.Authorizations;
import io.fabric8.launcher.core.api.security.Secured;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.PublicKeyProvider;

import static io.fabric8.launcher.base.http.Authorizations.isBearerAuthentication;
import static io.fabric8.launcher.core.impl.CoreEnvironment.LAUNCHER_KEYCLOAK_URL;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.fromHeaderValue;
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

    @Inject
    private PublicKeyProvider publicKeyProvider;

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
            if (shouldValidate(requestContext)) {
                validateToken(jwt);
            }
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

    // We do not validate tokens in case no keycloak linked for standalone launcher
    private boolean shouldValidate(ContainerRequestContext context) {
        if (Application.ApplicationType.LAUNCHER.equals(fromHeaderValue(context.getHeaderString(Application.APP_HEADER)))) {
            return LAUNCHER_KEYCLOAK_URL.isSet();
        }
        return true;
    }

    private void validateToken(DecodedJWT jwt) {
        final JWTValidator jwtValidator = new JWTValidator(jwt.getIssuer(), publicKeyProvider);
        if (!jwtValidator.validate(jwt.getToken())) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(status(UNAUTHORIZED).build());
    }
}
