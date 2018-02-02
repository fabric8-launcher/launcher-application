package io.fabric8.launcher.core.spi;

import java.nio.file.Path;

import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface Launcher {

    // Grab booster from catalog
    // Copy booster content to a temp file
    Path extractBooster(Projectile projectile);

    // Detect project type
    //  If Maven:
    //      - Change groupId/artifactId/version and build profile
    //      - Change child modules if they exist
    //  If Node.js:
    //      - Change name and version in package.json
    // Change README
    Path performCodeChanges(Projectile projectile, Path path);

    //--------------------------------------------------
    // GITHUB_CREATE - Create Github repository
    GitRepository createGithubRepository(Projectile p);

    // GITHUB_PUSHED - Push to Github
    GitRepository createGithubRepository(Projectile p, Path path, GitRepository gitRepository);

    // OPENSHIFT_CREATE - Create OpenShift project
    OpenShiftProject createOpenShiftProject(Projectile projectile);

    // OPENSHIFT_PIPELINE - Deploy Openshift resources
    void createPipelines(Projectile p);

    // GITHUB_WEBHOOK - Create Github webhooks
    void createWebhooks(Projectile projectile, OpenShiftProject project, GitRepository gitRepository);


}
