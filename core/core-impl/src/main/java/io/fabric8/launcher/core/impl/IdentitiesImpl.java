package io.fabric8.launcher.core.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.api.Identities;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import io.fabric8.launcher.service.keycloak.api.KeycloakService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class IdentitiesImpl implements Identities {

    @Inject
    private GitHubServiceFactory gitHubServiceFactory;

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    private Instance<KeycloakService> keycloakServiceInstance;

    @Override
    public Identity getGitHubIdentity(String authorization) {
        return gitHubServiceFactory.getDefaultIdentity().orElseGet(
                () -> keycloakServiceInstance.get().getGitHubIdentity(authorization)
        );
    }

    @Override
    public Identity getOpenShiftIdentity(String authorization, String cluster) {
        return openShiftServiceFactory.getDefaultIdentity().orElseGet(
                () -> {
                    KeycloakService keycloakService = keycloakServiceInstance.get();
                    if (cluster == null) {
                        return keycloakService.getOpenShiftIdentity(authorization);
                    } else {
                        return keycloakService.getIdentity(cluster, authorization)
                                .orElse(null);
                    }
                }
        );
    }
}