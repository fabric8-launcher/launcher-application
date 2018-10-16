package io.fabric8.launcher.core.impl.filters;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class JWTSecurityContext implements SecurityContext {

    private final JWTPrincipal principal;

    JWTSecurityContext(DecodedJWT jwt) {
        this.principal = new JWTPrincipal(jwt.getClaim("preferred_username").asString());
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return "JWT";
    }


    private class JWTPrincipal implements Principal {

        private final String name;

        private JWTPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
