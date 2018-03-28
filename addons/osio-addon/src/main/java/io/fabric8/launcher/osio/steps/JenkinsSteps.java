package io.fabric8.launcher.osio.steps;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.fabric8.launcher.osio.jenkins.JenkinsGitCredentials;
import io.fabric8.launcher.service.git.api.GitService;

@Dependent
public class JenkinsSteps {

    @Inject
    private GitService gitService;

    @Inject
    private JenkinsGitCredentials jenkinsGitCredentials;


    public void ensureJenkinsCDCredentialCreated() {
        jenkinsGitCredentials.ensureCredentials(gitService.getLoggedUser().getLogin());
    }
}