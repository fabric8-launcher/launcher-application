package io.fabric8.launcher.web;

import javax.enterprise.inject.spi.Extension;

import io.fabric8.launcher.web.PackageMarker;
import io.fabric8.launcher.web.api.LaunchResource;
import io.fabric8.launcher.web.forge.ForgeInitializer;
import io.fabric8.launcher.web.forge.cdi.LauncherExtension;
import io.fabric8.launcher.web.producers.GitServiceProducer;
import io.fabric8.launcher.web.providers.DirectoryReaperImpl;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
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
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class, LauncherExtension.class)
                .addPackages(true,
                             PackageMarker.class.getPackage())
                .addAsLibraries(Maven.resolver()
                                        .loadPomFromFile("pom.xml")
                                        .importCompileAndRuntimeDependencies()
                                        .resolve().withTransitivity().asFile());
    }
}
