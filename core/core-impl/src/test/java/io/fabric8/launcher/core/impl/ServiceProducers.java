package io.fabric8.launcher.core.impl;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class ServiceProducers {

    @Produces
    OpenShiftService getOpenShiftService(OpenShiftServiceFactory factory) {
        return factory.create();
    }


    @Produces
    GitService getGitService(GitHubServiceFactory factory) {
        return factory.create();
    }
}
