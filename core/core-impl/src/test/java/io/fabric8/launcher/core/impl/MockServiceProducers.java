package io.fabric8.launcher.core.impl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.base.Paths;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalogService;
import io.fabric8.launcher.core.api.DirectoryReaper;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.spi.GitProvider;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

import static io.fabric8.launcher.service.git.spi.GitProvider.GitProviderType.GITHUB;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class MockServiceProducers {

    private static final Logger log = Logger.getLogger(MockServiceProducers.class.getName());

    @Produces
    OpenShiftService getOpenShiftService(OpenShiftServiceFactory factory) {
        return factory.create();
    }


    @Produces
    GitService getGitService(@GitProvider(GITHUB) GitServiceFactory factory) {
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
                Paths.deleteDirectory(path);
            } catch (IOException e) {
                log.log(Level.WARNING, "Error deleting directory: " + path, e);
            }
        };
    }
}
