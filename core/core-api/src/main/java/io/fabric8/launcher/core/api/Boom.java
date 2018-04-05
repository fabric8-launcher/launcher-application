package io.fabric8.launcher.core.api;

import io.fabric8.launcher.core.api.projectiles.CreateProjectile;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import org.immutables.value.Value;

/**
 * Value object containing the result of a {@link MissionControl#launch(CreateProjectile)}
 * call.  Implementations should be immutable and therefore thread-safe.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@Value.Immutable
public interface Boom {
    /**
     * @return the repository we've created for the user
     */
    GitRepository getCreatedRepository();

    /**
     * @return the OpenShift project we've created for the user
     */
    OpenShiftProject getCreatedProject();
}
