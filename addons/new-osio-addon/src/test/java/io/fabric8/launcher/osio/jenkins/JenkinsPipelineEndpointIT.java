package io.fabric8.launcher.osio.jenkins;


import java.net.URI;

import io.fabric8.launcher.osio.HttpApplication;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE;
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME;
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST;

@RunWith(Arquillian.class)
@RunAsClient
public class JenkinsPipelineEndpointIT {

    @ArquillianResource
    protected URI deploymentUri;

    private RequestSpecification configureEndpoint() {
        return new RequestSpecBuilder().setBaseUri(URI.create(deploymentUri + "api/services/jenkins")).build();
    }

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackages(true,
                             JenkinsPipelineEndpoint.class.getPackage())
                .addClass(HttpApplication.class)
                .addAsLibraries(Maven.resolver()
                                        .loadPomFromFile("pom.xml")
                                        .importDependencies(RUNTIME, COMPILE, TEST)
                                        .resolve().withTransitivity().asFile());
    }

    @Test
    public void shouldSendSomething() {
        given()
                .spec(configureEndpoint())
                .when()
                .get("/pipelines")
                .then()
                .assertThat().statusCode(200)
                .body("name[0]", is("Release and Stage"));

    }
}
