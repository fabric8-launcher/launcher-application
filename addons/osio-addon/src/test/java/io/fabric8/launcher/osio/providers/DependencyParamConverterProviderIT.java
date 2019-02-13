package io.fabric8.launcher.osio.providers;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Disabled
public class DependencyParamConverterProviderIT {

    @Test
    public void should_convert_dependency() {
        given()
                .queryParam("dependency", "org.foo:bar:1.0")
                .when()
                .get("/api/converters")
                .then()
                .assertThat().statusCode(200)
                .body(containsString("org.foo:bar:1.0"));
    }

    @Test
    public void should_convert_dependencies() {
        given()
                .queryParam("dependency", "org.foo:bar:1.0")
                .queryParam("dependency", "org.acme:acme:2.0")
                .when()
                .get("/api/converters")
                .then()
                .assertThat().statusCode(200)
                .body(allOf(containsString("org.foo:bar:1.0"), containsString("org.acme:acme:2.0")));
    }


    @Test
    public void should_return_empty_if_no_dependency_is_sent() {
        given()
                .when()
                .get("/api/converters")
                .then()
                .assertThat().statusCode(200)
                .body(isEmptyString());
    }

    @Test
    public void should_treat_malformed_dependencies_as_bad_request() {
        given()
                .queryParam("dependency", "foo")
                .when()
                .get("/api/converters")
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
