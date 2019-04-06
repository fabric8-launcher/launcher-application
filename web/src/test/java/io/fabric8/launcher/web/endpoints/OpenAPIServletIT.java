package io.fabric8.launcher.web.endpoints;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class OpenAPIServletIT {

    @Test
    void should_expose_openapi_endpoint() {
        given()
                .when()
                    .get("/openapi")
                .then()
                    .statusCode(200)
                    .contentType("application/yaml");

    }
}
