package io.fabric8.launcher.osio.steps;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import io.fabric8.launcher.osio.projectiles.OsioProjectile;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class GitSteps {

    @Inject
    private GitService gitService;


    public GitRepository createRepository(OsioProjectile projectile) {
        return null;
    }


}
