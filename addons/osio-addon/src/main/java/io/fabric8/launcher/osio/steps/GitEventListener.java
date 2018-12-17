package io.fabric8.launcher.osio.steps;

import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.service.git.api.GitRepository;

public interface GitEventListener {

    void pushToGitRepositoryCompleted(OsioProjectile projectile, GitRepository repository);
}
