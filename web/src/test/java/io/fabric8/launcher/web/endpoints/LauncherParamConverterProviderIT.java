package io.fabric8.launcher.web.endpoints;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.web.Deployments;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class LauncherParamConverterProviderIT {

    @ArquillianResource
    URI deploymentUri;

    public RequestSpecification configureEndpoint() {
        return new RequestSpecBuilder().setBaseUri(UriBuilder.fromUri(deploymentUri).path("api").path("converters").build()).build();
    }


    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.createDeployment().addClass(RhoarBoosterEndpoint.class);
    }

    @Before
    public void wait_until_catalog_is_indexed() {
        given()
                .spec(new RequestSpecBuilder().setBaseUri(UriBuilder.fromUri(deploymentUri).path("api").path("booster-catalog").build()).build())
                .when()
                .get("/wait")
                .then()
                .assertThat().statusCode(200);

    }

    @Test
    public void should_convert_mission() {
        given()
                .spec(configureEndpoint())
                .queryParam("id", "crud")
                .when()
                .get("/mission")
                .then()
                .assertThat().statusCode(200)
                .body(containsString("CRUD"));
    }

    @Test
    public void should_convert_runtime() {
        given()
                .spec(configureEndpoint())
                .queryParam("id", "vert.x")
                .when()
                .get("/runtime")
                .then()
                .assertThat().statusCode(200)
                .body(containsString("Eclipse Vert.x"));

    }

    @Test
    public void should_treat_unknown_missions_as_bad_request() {
        given()
                .spec(configureEndpoint())
                .queryParam("id", "foo")
                .when()
                .get("/mission")
                .then()
                .assertThat().statusCode(400);

    }

    @Test
    public void should_treat_unknown_runtimes_as_bad_request() {
        given()
                .spec(configureEndpoint())
                .queryParam("id", "foo")
                .when()
                .get("/runtime")
                .then()
                .assertThat().statusCode(400);

    }

    @Path("/converters")
    public static class RhoarBoosterEndpoint {

        @GET
        @Path("/mission")
        @Produces(MediaType.TEXT_PLAIN)
        public String getMission(@QueryParam("id") Mission mission) {
            return mission.getName();
        }

        @GET
        @Path("/runtime")
        @Produces(MediaType.TEXT_PLAIN)
        public String getMission(@QueryParam("id") Runtime runtime) {
            return runtime.getName();
        }
    }
}
