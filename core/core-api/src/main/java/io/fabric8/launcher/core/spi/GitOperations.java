package io.fabric8.launcher.core.spi;

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;

/**
 * The operations done by Mission control
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface GitOperations {
    GitRepository createGitRepository(CreateProjectile projectile);

    void pushToGitRepository(CreateProjectile projectile, GitRepository repository);

    void createWebHooks(CreateProjectile projectile, OpenShiftProject openShiftProject, GitRepository gitRepository);
}
