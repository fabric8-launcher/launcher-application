package io.fabric8.launcher.core.impl;

import java.io.File;

import com.fasterxml.jackson.databind.MapperFeature;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThatCode;

@RunWith(Arquillian.class)
public class JacksonVersionOverrideIT {

    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.createDeployment()
                .addAsWebInfResource(new File("src/main/resources/META-INF/beans.xml"), "beans.xml")
                .addClass(MockServiceProducers.class);
    }

    @Test
    public void should_use_jackson_2_9() {
        // Check if Jackson 2.9.x is used instead of 2.8.x by testing if an enum value that exists only in 2.9 is available
        assertThatCode(() -> MapperFeature.valueOf("ACCEPT_CASE_INSENSITIVE_ENUMS"))
                .doesNotThrowAnyException();
    }


}
