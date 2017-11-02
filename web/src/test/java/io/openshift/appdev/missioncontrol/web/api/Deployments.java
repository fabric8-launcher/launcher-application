package io.openshift.appdev.missioncontrol.web.api;

import javax.enterprise.inject.spi.Extension;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import io.openshift.launchpad.backend.ForgeInitializer;
import io.openshift.launchpad.backend.cdi.LaunchpadExtension;

/**
 * Util class that creates a shrinkwrap deployment used to create IT tests.
 */
public class Deployments
{

   /**
    * Create war archive to test.
    * 
    * @return the war used for testing
    */
   public static Archive<?> createDeployment()
   {
      return ShrinkWrap.create(JAXRSArchive.class)
               .addAsWebInfResource("META-INF/beans.xml", "beans.xml")
               .addAsServiceProvider(Extension.class, LaunchpadExtension.class)
               .addPackages(true, ForgeInitializer.class.getPackage().getName())
               .addAsLibraries(Maven.resolver()
                        .loadPomFromFile("pom.xml")
                        .importCompileAndRuntimeDependencies()
                        .resolve().withTransitivity().asFile());
   }
}
