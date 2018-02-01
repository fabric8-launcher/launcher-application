package io.fabric8.launcher.osio.jenkins;


import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.arquillian.smart.testing.rules.git.server.GitServer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

@RunWith(Arquillian.class)
@RunAsClient
public class JenkinsPipelineResourceIT {

    @ClassRule
    public static GitServer gitServer = GitServer
            .fromBundle("fabric8-jenkinsfile-library", "repos/fabric8-jenkinsfile-library.bundle")
            .usingPort(8765)
            .create();


    @ArquillianResource
    protected URI deploymentUri;

    private RequestSpecification configureEndpoint() {
        return new RequestSpecBuilder().setBaseUri(
                UriBuilder.fromUri(deploymentUri).path("api").path("services").path("jenkins").build()).build();
    }

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackages(true,
                             JenkinsPipelineResource.class.getPackage())
                .addAsLibraries(Maven.resolver()
                                        .loadPomFromFile("pom.xml")
                                        .importCompileAndRuntimeDependencies()
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
                .body("name[0]", is("name"));

    }
}
