package io.fabric8.launcher.osio.providers;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.osio.EnvironmentVariables;

import static io.fabric8.launcher.base.identity.IdentityFactory.createFromToken;
import static io.fabric8.launcher.base.identity.TokenIdentity.removeBearerPrefix;

@RequestScoped
public class KubernetesClientProvider {

    @Produces
    public KubernetesClient createOpenshiftClient(HttpServletRequest request) {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        TokenIdentity token = createFromToken(removeBearerPrefix(authorization));

        String openShiftApiURL = EnvironmentVariables.getOpenShiftApiURL();
        Config config = new ConfigBuilder().withMasterUrl(openShiftApiURL).withOauthToken(token.getToken())
                .withTrustCerts(true).build();
        return new DefaultKubernetesClient(config);
    }
}
