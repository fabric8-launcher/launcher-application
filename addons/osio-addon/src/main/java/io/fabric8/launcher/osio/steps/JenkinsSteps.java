package io.fabric8.launcher.osio.steps;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.osio.client.OsioJenkinsClient;
import io.fabric8.launcher.service.git.api.GitService;

@Dependent
public class JenkinsSteps {

    @Inject
    private GitService gitService;

    @Inject
    private OsioJenkinsClient osioJenkinsClient;


    public void ensureJenkinsCDCredentialCreated() {
        osioJenkinsClient.ensureCredentials(gitService.getLoggedUser().getLogin(), gitService.getIdentity());
    }
}