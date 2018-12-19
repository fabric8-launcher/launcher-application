package io.fabric8.launcher.osio.steps;

import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.service.git.api.GitRepository;

/**
 * An interface used to for notifications related
 * to git steps.
 *
 */
public interface GitEventListener {

    /**
     * The callback to be implemented by listeners who are
     * interested in knowing when gitSteps.pushToGitRepository()
     * has been completed.
     *
     * @param projectile
     *          The projectile used. Used to create web hooks.
     * @param repository
     *          The git repository to be used. Used to create web hooks.
     */
    void pushEventNotification(OsioProjectile projectile, GitRepository repository);
}
