package io.fabric8.launcher.core.impl.filters;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.fabric8.launcher.base.http.Authorizations;
import io.fabric8.launcher.core.api.security.Secured;

import static io.fabric8.launcher.base.http.Authorizations.isBearerAuthentication;

/**
 * Based on https://stackoverflow.com/questions/26777083/best-practice-for-rest-token-based-authentication-with-jax-rs-and-jersey
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecuredFilter implements ContainerRequestFilter {

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

            // Validate the token
            DecodedJWT jwt = validateToken(token);
            JWTSecurityContext securityContext = new JWTSecurityContext(jwt);
            // Set the user name as a request property
            requestContext.setProperty("USER_NAME", securityContext.getUserPrincipal().getName());
            requestContext.setSecurityContext(securityContext);

        } catch (Exception e) {
            abortWithUnauthorized(requestContext);
        }
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {

        // Abort the filter chain with a 401 status code response
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .build());
    }

    private DecodedJWT validateToken(String token) {
        // TODO: Check if the token was issued by the server and if it's not expired
        return JWT.decode(token);
    }
}
