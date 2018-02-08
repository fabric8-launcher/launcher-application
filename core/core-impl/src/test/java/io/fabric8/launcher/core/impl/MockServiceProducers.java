package io.fabric8.launcher.core.impl;

import java.io.IOException;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.booster.Files;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalogService;
import io.fabric8.launcher.core.api.DirectoryReaper;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class MockServiceProducers {

    @Produces
    OpenShiftService getOpenShiftService(OpenShiftServiceFactory factory) {
        return factory.create();
    }


    @Produces
    GitService getGitService(GitHubServiceFactory factory) {
        return factory.create();
    }

    @Produces
    RhoarBoosterCatalog getRhoarCatalog() {
        return new RhoarBoosterCatalogService.Builder()
                .catalogRef("master")
                .build();
    }

    @Produces
    DirectoryReaper getDirectoryReaper() {
        return (path) -> {
            try {
                Files.deleteRecursively(path);
            } catch (IOException e) {
                //ignore
            }
        };
    }
}
