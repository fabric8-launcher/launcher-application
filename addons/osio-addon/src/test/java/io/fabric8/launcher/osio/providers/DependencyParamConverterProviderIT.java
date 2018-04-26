package io.fabric8.launcher.osio.providers;

import java.io.File;
import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.fabric8.launcher.base.test.HttpApplication;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.apache.maven.model.Dependency;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.given;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class DependencyParamConverterProviderIT {

    @ArquillianResource
    URI deploymentUri;

    public RequestSpecification configureEndpoint() {
        return new RequestSpecBuilder().setBaseUri(URI.create(deploymentUri + "api/converters")).build();
    }


    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(new FileAsset(new File("src/main/resources/META-INF/beans.xml")), "beans.xml")
                .addClasses(DependencyEndpoint.class, HttpApplication.class)
                .addPackage(DependencyParamConverterProvider.class.getPackage())
                .addAsLibraries(Maven.resolver()
                                        .loadPomFromFile("pom.xml")
                                        .importDependencies(COMPILE)
                                        .resolve().withTransitivity().asFile());
    }


    @Test
    public void should_convert_dependency() {
        given()
                .spec(configureEndpoint())
                .queryParam("dependency", "org.foo:bar:1.0")
                .when()
                .get()
                .then()
                .assertThat().statusCode(200)
                .body(containsString("org.foo:bar:1.0"));
    }

    @Test
    public void should_convert_dependencies() {
        given()
                .spec(configureEndpoint())
                .queryParam("dependency", "org.foo:bar:1.0")
                .queryParam("dependency", "org.acme:acme:2.0")
                .when()
                .get()
                .then()
                .assertThat().statusCode(200)
                .body(allOf(containsString("org.foo:bar:1.0"), containsString("org.acme:acme:2.0")));
    }


    @Test
    public void should_return_empty_if_no_dependency_is_sent() {
        given()
                .spec(configureEndpoint())
                .when()
                .get()
                .then()
                .assertThat().statusCode(200)
                .body(isEmptyString());
    }

    @Test
    public void should_treat_malformed_dependencies_as_bad_request() {
        given()
                .spec(configureEndpoint())
                .queryParam("dependency", "foo")
                .when()
                .get()
                .then()
                .assertThat().statusCode(400);

    }

    @Path("/converters")
    public static class DependencyEndpoint {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String getDependency(@QueryParam("dependency") List<Dependency> dependencies) {
            return dependencies
                    .stream()
                    .map((dependency) -> dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
                    .collect(joining(","));
        }

    }
}
