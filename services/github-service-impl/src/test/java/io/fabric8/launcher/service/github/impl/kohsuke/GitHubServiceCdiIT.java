package io.fabric8.launcher.service.github.impl.kohsuke;


import java.io.File;
import java.util.logging.Logger;

import javax.inject.Inject;

import io.fabric8.launcher.service.github.api.GitHubService;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import io.fabric8.launcher.service.github.spi.GitHubServiceSpi;
import io.fabric8.launcher.service.github.test.GitHubTestCredentials;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.runner.RunWith;

/**
 * Integration Tests for the {@link GitHubService}
 *
 * Relies on having environment variables set for:
 * GITHUB_USERNAME
 * GITHUB_TOKEN
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@RunWith(Arquillian.class)
public final class GitHubServiceCdiIT extends GitHubServiceTestBase {

    private static final Logger log = Logger.getLogger(GitHubServiceCdiIT.class.getName());

    @Inject
    private GitHubServiceFactory gitHubServiceFactory;

    /**
     * @return a war file containing all the required classes and dependencies
     * to test the {@link GitHubService}
     */
    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        // Import Maven runtime dependencies
        final File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();
        // Create deploy file
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackage(KohsukeGitHubServiceFactoryImpl.class.getPackage())
                .addClass(GitHubTestCredentials.class)
                .addClass(GitHubServiceSpi.class)
                // libraries will include all classes/interfaces from the API project.
                .addAsLibraries(dependencies);
        // Show the deployed structure
        log.fine(war.toString(true));
        return war;
    }

    @Override
    protected GitHubService getGitHubService() {
        return gitHubServiceFactory.create(GitHubTestCredentials.getToken());
    }
}
