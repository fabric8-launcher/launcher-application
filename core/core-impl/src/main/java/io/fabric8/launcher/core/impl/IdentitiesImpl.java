package io.fabric8.launcher.core.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.api.Identities;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import io.fabric8.launcher.service.keycloak.api.KeycloakService;
import io.fabric8.launcher.service.keycloak.api.KeycloakServiceFactory;
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
    private KeycloakServiceFactory keycloakServiceFactory;

    @Override
    public Identity getGitHubIdentity(String authorization) {
        return gitHubServiceFactory.getDefaultIdentity().orElseGet(
                () -> keycloakServiceFactory.create().getGitHubIdentity(authorization)
        );
    }

    @Override
    public Identity getOpenShiftIdentity(String authorization, String cluster) {
        return openShiftServiceFactory.getDefaultIdentity().orElseGet(
                () -> {
                    KeycloakService keycloakService = keycloakServiceFactory.create();
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