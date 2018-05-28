package io.fabric8.launcher.web;

import javax.enterprise.inject.spi.Extension;

import io.fabric8.launcher.web.forge.cdi.LauncherExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Util class that creates a shrinkwrap deployment used to create IT tests.
 */
public class Deployments {

    /**
     * Create war archive to test.
     *
     * @return the war used for testing
     */
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"annotated\" version=\"1.1\"/>"),
                                     "beans.xml")
                .addAsServiceProvider(Extension.class, LauncherExtension.class)
                .addPackages(true, PackageMarker.class.getPackage())
                .addAsLibraries(Maven.resolver()
                                        .loadPomFromFile("pom.xml")
                                        .importCompileAndRuntimeDependencies()
                                        .resolve().withTransitivity().asFile());
    }
}
