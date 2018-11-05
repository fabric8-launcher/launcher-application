package io.fabric8.launcher.web;

import java.io.File;
import java.util.Objects;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import static java.util.Objects.requireNonNull;

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

        WebArchive archive = ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"annotated\" version=\"1.1\"/>"),
                                     "beans.xml")
                .addPackages(true, PackageMarker.class.getPackage())
                .addAsLibraries(Maven.resolver()
                                        .loadPomFromFile("pom.xml")
                                        .importCompileAndRuntimeDependencies()
                                        .resolve().withTransitivity().asFile());

        // Add META-INF files
        for (File file: requireNonNull(new File("src/main/resources/META-INF").listFiles())) {
            archive.addAsManifestResource(file);
        }
        return archive;
    }
}
