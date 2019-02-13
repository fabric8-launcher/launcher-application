package io.fabric8.launcher.web.filters;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.fabric8.launcher.base.test.identity.TokenFixtures.OUTDATED_TOKEN;
import static io.fabric8.launcher.base.test.identity.TokenFixtures.TOKEN_SIGNED_WITH_DIFFERENT_KEY;
import static io.fabric8.launcher.base.test.identity.TokenFixtures.VALID_TOKEN;
import static io.restassured.RestAssured.given;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@QuarkusTest
class SecuredIT {

    @Test
    void should_return_401_in_annotated_endpoints_without_token() {
        given()
                .when()
                .get("/api/endpoint/secured")
                .then()
                .assertThat().statusCode(401);

    }

    @Test
    void should_return_401_in_annotated_endpoints_with_invalid_authorization_header_token() {
        given()
                .when()
                .header("Authorization", "xyz")
                .get("/api/endpoint/secured")
                .then()
                .assertThat().statusCode(401);

    }

    @Test
    void should_return_401_when_token_is_outdated() {
        given()
                .when()
                .header("Authorization", "Bearer " + OUTDATED_TOKEN)
                .get("/api/endpoint/secured")
                .then()
                .assertThat().statusCode(401);
    }

    @Test
    void should_return_401_when_token_is_signed_by_different_key() {
        given()
                .when()
                .header("Authorization", "Bearer " + TOKEN_SIGNED_WITH_DIFFERENT_KEY)
                .get("/api/endpoint/secured")
                .then()
                .assertThat().statusCode(401);
    }

    @Test
    void should_return_200_in_annotated_endpoints_with_token() {
        given()
                .when()
                .header("Authorization", "Bearer " + VALID_TOKEN)
                .get("/api/endpoint/secured")
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    void should_not_secure_not_annotated_endpoints() {
        given()
                .when()
                .get("/api/endpoint/insecured")
                .then()
                .assertThat().statusCode(200);
    }

}
