package io.fabric8.launcher.core.impl;

import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.service.github.test.GitHubTestCredentials;
import io.fabric8.launcher.service.openshift.test.OpenShiftTestCredentials;
import io.fabric8.launcher.tracking.SegmentAnalyticsProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Util class that creates a shrinkwrap deployment used to create IT tests.
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class Deployments {

    /**
     * @return a war file containing all the required classes and dependencies
     * to test the {@link MissionControl}
     */
    public static WebArchive createDeployment() {
        // Create deploy file
        return ShrinkWrap.create(WebArchive.class)
                .addPackages(true, MissionControl.class.getPackage())
                .addPackages(true, MissionControlImpl.class.getPackage())
                .addPackages(true, Application.class.getPackage())
                .addPackages(true, SegmentAnalyticsProvider.class.getPackage())
                .addClasses(GitHubTestCredentials.class, OpenShiftTestCredentials.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml")
                                        .importCompileAndRuntimeDependencies().resolve().withTransitivity().asFile());
    }

}
