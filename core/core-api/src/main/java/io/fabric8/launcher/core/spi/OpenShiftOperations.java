package io.fabric8.launcher.core.spi;

import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface OpenShiftOperations {
    OpenShiftProject createOpenShiftProject(CreateProjectile projectile);

    void configureBuildPipeline(CreateProjectile projectile, OpenShiftProject openShiftProject, GitRepository gitRepository);
}
