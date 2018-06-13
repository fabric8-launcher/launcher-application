package io.fabric8.launcher.core.impl;

import com.fasterxml.jackson.databind.MapperFeature;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThatCode;

@RunWith(Arquillian.class)
@Ignore("Ignore until WildFly Swarm supports it")
public class JacksonVersionOverrideIT {

    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.createDeployment()
                .addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml")
                                      .importTestDependencies().resolve().withTransitivity().asFile())
                .addClass(MockServiceProducers.class);
    }

    @Test
    public void should_use_jackson_2_9() {
        // Check if Jackson 2.9.x is used instead of 2.8.x by testing if an enum value that exists only in 2.9 is available
        assertThatCode(() -> MapperFeature.valueOf("ACCEPT_CASE_INSENSITIVE_ENUMS"))
                .doesNotThrowAnyException();
    }


}
