package io.fabric8.launcher.core.impl;

import java.util.List;

import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.github.api.GitHubRepository;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public final class BoomImpl implements Boom {

    /**
     * Creates a new instance with the specified, required {@link GitHubRepository}
     * and {@link OpenShiftProject}
     *
     * @param gitHubRepository the forked repository on GitHub. Required
     * @param openShiftProject the project created on OpenShift. Required
     * @param webhooks         the webhook created on the forked repo on GitHub to trigger builds on OpenShift. Optional
     */
    BoomImpl(final GitRepository gitHubRepository, final OpenShiftProject openShiftProject, final List<GitHook> webhooks) {
        assert gitHubRepository != null : "gitHubRepository must be specified";
        assert openShiftProject != null : "openShiftProject must be specified";
        this.gitHubRepository = gitHubRepository;
        this.openShiftProject = openShiftProject;
        this.webhooks = webhooks;
    }

    private final GitRepository gitHubRepository;

    private final OpenShiftProject openShiftProject;

    private final List<GitHook> webhooks;

    @Override
    public GitRepository getCreatedRepository() {
        return this.gitHubRepository;
    }

    @Override
    public OpenShiftProject getCreatedProject() {
        return this.openShiftProject;
    }

    @Override
    public List<GitHook> getGitHubWebhooks() {
        return this.webhooks;
    }
}
