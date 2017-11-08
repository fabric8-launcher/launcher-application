package io.openshift.appdev.missioncontrol.core.api;

import io.openshift.appdev.missioncontrol.service.github.api.GitHubWebhook;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;

import java.util.List;

import io.openshift.appdev.missioncontrol.service.github.api.GitHubRepository;

/**
 * Value object containing the result of a {@link MissionControl#fling(Projectile)}
 * call.  Implementations should be immutable and therefore thread-safe.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface Boom {

    /**
     * @return the repository we've created for the user
     */
    GitHubRepository getCreatedRepository();

    /**
     * @return the OpenShift project we've created for the user
     */
    OpenShiftProject getCreatedProject();

    /**
     * @return the list of webhooks created on the forked repo on GitHub to trigger
     * builds on OpenShift.
     */
    List<GitHubWebhook> getGitHubWebhooks();

}
