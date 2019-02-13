package io.fabric8.launcher.web.filters;

import io.fabric8.launcher.core.impl.CoreEnvironment;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.fabric8.launcher.base.test.identity.TokenFixtures.TOKEN_SIGNED_WITH_DIFFERENT_KEY;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * This test relies on LAUNCHER_KEYCLOAK_URL being unset
 */
@QuarkusTest
class NoKeycloakUrlSetSecuredIT {

    @Test
    void should_return_skip_validation_for_any_token() {
        assumeFalse(CoreEnvironment.LAUNCHER_KEYCLOAK_URL.isSet());
        given()
                .when()
                .header("Authorization", "Bearer " + TOKEN_SIGNED_WITH_DIFFERENT_KEY)
                .get("/api/endpoint/secured")
                .then()
                .assertThat().statusCode(200);
    }
}