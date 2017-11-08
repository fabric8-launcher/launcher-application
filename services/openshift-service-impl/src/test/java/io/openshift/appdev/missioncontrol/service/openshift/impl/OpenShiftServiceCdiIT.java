package io.openshift.appdev.missioncontrol.service.openshift.impl;

import java.io.File;
import java.util.logging.Logger;

import javax.inject.Inject;

import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftCluster;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftClusterRegistry;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftServiceFactory;
import io.openshift.appdev.missioncontrol.service.openshift.impl.fabric8.openshift.client.Fabric8OpenShiftServiceImpl;
import io.openshift.appdev.missioncontrol.service.openshift.spi.OpenShiftServiceSpi;
import io.openshift.appdev.missioncontrol.service.openshift.test.OpenShiftTestCredentials;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de
 * Oliveira</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
@RunWith(Arquillian.class)
public class OpenShiftServiceCdiIT extends OpenShiftServiceTestBase {

    private static final Logger log = Logger.getLogger(OpenShiftServiceCdiIT.class.getName());

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    /**
     * @return a jar file containing all the required classes to test the {@link OpenShiftService}
     */
    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        // Import Maven runtime dependencies
        final File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
        // Create deploy file
        final WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackage(Fabric8OpenShiftServiceImpl.class.getPackage())
                .addPackage(OpenShiftServiceCdiIT.class.getPackage())
                .addPackage(OpenShiftService.class.getPackage())
                .addClass(DeleteOpenShiftProjectRule.class)
                .addClass(OpenShiftServiceSpi.class)
                .addClass(OpenShiftTestCredentials.class)
                .addClasses(OpenShiftCluster.class, OpenShiftClusterRegistry.class, OpenShiftClusterRegistryImpl.class, OpenShiftClusterConstructor.class)
                .addAsResource("openshift-project-template.json")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("META-INF/jboss-deployment-structure.xml", "jboss-deployment-structure.xml")
                .addAsLibraries(dependencies);
        // Show the deployed structure
        log.info(war.toString(true));
        return war;
    }

    @Override
    public OpenShiftService getOpenShiftService() {
        return this.openShiftServiceFactory.create(OpenShiftTestCredentials.getIdentity());
    }

}
