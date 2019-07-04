package io.fabric8.launcher.web.providers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@QuarkusTest
public class LauncherParamConverterProviderIT {

    @BeforeEach
    void wait_until_catalog_is_indexed() {
        given()
                .when()
                .get("/api/booster-catalog/wait")
                .then()
                .assertThat().statusCode(200);

    }

    @Test
    void should_convert_mission() {
        given()
                .queryParam("id", "crud")
                .when()
                .get("/api/converters/mission")
                .then()
                .assertThat().statusCode(200)
                .body(containsString("CRUD"));
    }

    @Test
    void should_convert_runtime() {
        given()
                .queryParam("id", "vert.x")
                .when()
                .get("/api/converters/runtime")
                .then()
                .assertThat().statusCode(200)
                .body(containsString("Eclipse Vert.x"));

    }

    @Test
    void should_convert_jsonnode() {
        given()
                .queryParam("id", "{\"id\":\"a\"}")
                .when()
                .get("/api/converters/jsonnode")
                .then()
                .assertThat().statusCode(200)
                .body(containsString("a"));

    }

    @Test
    void should_convert_objectnode() {
        given()
                .queryParam("id", "{\"id\":\"a\"}")
                .when()
                .get("/api/converters/objectnode")
                .then()
                .assertThat().statusCode(200)
                .body(is("a"));

    }

    @Test
    void should_convert_arraynode() {
        given()
                .queryParam("id", "[\"a\",\"b\"]")
                .when()
                .get("/api/converters/arraynode")
                .then()
                .assertThat().statusCode(200)
                .body(is( "2"));
    }

    @Test
    void should_treat_unknown_missions_as_not_found() {
        given()
                .queryParam("id", "foo")
                .when()
                .get("/api/converters/mission")
                .then()
                .assertThat().statusCode(404);

    }

    @Test
    void should_treat_unknown_runtimes_as_not_found() {
        given()
                .queryParam("id", "foo")
                .when()
                .get("/api/converters/runtime")
                .then()
                .assertThat().statusCode(404);

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
        public String getRuntime(@QueryParam("id") Runtime runtime) {
            return runtime.getName();
        }

        @GET
        @Path("/jsonnode")
        @Produces(MediaType.TEXT_PLAIN)
        public String getJsonNode(@QueryParam("id") JsonNode node) {
            return node.get("id").asText();
        }

        @GET
        @Path("/objectnode")
        @Produces(MediaType.TEXT_PLAIN)
        public String getObjectNode(@QueryParam("id") ObjectNode node) {
            return node.get("id").asText();
        }

        @GET
        @Path("/arraynode")
        @Produces(MediaType.TEXT_PLAIN)
        public Integer getArrayNode(@QueryParam("id") ArrayNode node) {
            return node.size();
        }
    }
}
