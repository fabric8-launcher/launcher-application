package io.fabric8.forge.generator.kubernetes;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.WebApplicationException;

import io.fabric8.forge.generator.EnvironmentVariables;
import io.fabric8.forge.generator.keycloak.KeycloakEndpoint;
import io.fabric8.forge.generator.keycloak.TokenHelper;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.utils.Strings;
import org.jboss.forge.addon.ui.context.UIContext;

/**
 * Factory that constructs a KubernetesClient.
 */
@ApplicationScoped
public class KubernetesClientFactory {


    public KubernetesClientHelper createKubernetesClient(UIContext context) {
        return new KubernetesClientHelper(createKubernetesClientForSSO(context));
    }

    /**
     * Creates the kubernetes client for the SSO signed in user
     */
    private KubernetesClient createKubernetesClientForSSO(UIContext context) {
        String authHeader = TokenHelper.getMandatoryAuthHeader(context);
        String openshiftToken = TokenHelper.getMandatoryTokenFor(KeycloakEndpoint.GET_OPENSHIFT_TOKEN, authHeader);
        String openShiftApiUrl = System.getenv(EnvironmentVariables.OPENSHIFT_API_URL);
        if (Strings.isNullOrBlank(openShiftApiUrl)) {
            throw new WebApplicationException("No environment variable defined: "
                                                      + EnvironmentVariables.OPENSHIFT_API_URL + " so cannot connect to OpenShift Online!");
        }
        Config config = new ConfigBuilder().withMasterUrl(openShiftApiUrl).withOauthToken(openshiftToken).
                // TODO until we figure out the trust thing lets ignore warnings
                        withTrustCerts(true).
                        build();
        return new DefaultKubernetesClient(config);
    }

}
