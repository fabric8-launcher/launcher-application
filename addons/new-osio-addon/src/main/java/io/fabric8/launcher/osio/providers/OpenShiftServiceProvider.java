package io.fabric8.launcher.osio.providers;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.EnvironmentVariables;
import io.fabric8.launcher.osio.openshift.OpenShiftService;
import io.fabric8.launcher.osio.tenant.Tenant;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;

@RequestScoped
public class OpenShiftServiceProvider {

    @Produces
    @Application(OSIO)
    public OpenShiftService createOpenShiftService(HttpServletRequest request, IdentityProvider identityProvider, Tenant tenant) {
        String openShiftApiURL = EnvironmentVariables.getOpenShiftApiURL();
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        ConfigBuilder configBuilder = new ConfigBuilder();
        identityProvider.getIdentity(IdentityProvider.ServiceType.OPENSHIFT, authorization)
                .orElseThrow(IllegalArgumentException::new).accept(new IdentityVisitor() {
            @Override
            public void visit(TokenIdentity token) {
                configBuilder.withOauthToken(token.getToken());
            }

            @Override
            public void visit(UserPasswordIdentity userPassword) {
                configBuilder
                        .withUsername(userPassword.getUsername())
                        .withPassword(userPassword.getPassword());
            }
        });
        Config config = configBuilder.withMasterUrl(openShiftApiURL)
                .withTrustCerts(true).build();
        DefaultKubernetesClient client = new DefaultKubernetesClient(config);
        return new OpenShiftService(client, tenant);
    }
}
