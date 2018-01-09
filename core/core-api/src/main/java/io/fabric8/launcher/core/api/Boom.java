package io.fabric8.launcher.core.api;

import java.util.List;

import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;

/**
 * Value object containing the result of a {@link MissionControl#launch(CreateProjectile)}
 * call.  Implementations should be immutable and therefore thread-safe.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface Boom {

    /**
     * @return the repository we've created for the user
     */
    GitRepository getCreatedRepository();

    /**
     * @return the OpenShift project we've created for the user
     */
    OpenShiftProject getCreatedProject();

    /**
     * @return the list of webhooks created on the forked repo on GitHub to trigger
     * builds on OpenShift.
     */
    List<GitHook> getGitHubWebhooks();

}
