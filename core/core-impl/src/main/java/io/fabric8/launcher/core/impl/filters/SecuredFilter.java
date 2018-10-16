package io.fabric8.launcher.core.impl.filters;

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
import io.fabric8.launcher.core.spi.PublicKeyProvider;

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
            JWTSecurityContext securityContext = new JWTSecurityContext(jwt);
            // Set the user name as a request property
            requestContext.setProperty("USER_NAME", securityContext.getUserPrincipal().getName());
            requestContext.setSecurityContext(securityContext);

        } catch (Exception e) {
            abortWithUnauthorized(requestContext);
        }
    }

    private boolean shouldValidate(ContainerRequestContext requestContext) {
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
