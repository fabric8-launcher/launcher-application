package io.fabric8.launcher.core.impl.filters;

import java.net.URI;

import io.fabric8.launcher.core.impl.Deployments;
import io.fabric8.launcher.core.impl.MockServiceProducers;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.fabric8.launcher.base.test.identity.TokenFixtures.TOKEN_SIGNED_WITH_DIFFERENT_KEY;
import static io.restassured.RestAssured.given;

/**
 * This test relies on LAUNCHER_KEYCLOAK_URL being unset
 */
@RunWith(Arquillian.class)
@RunAsClient
public class NoKeycloakUrlSetSecuredIT {

    @ArquillianResource
    protected URI deploymentUri;

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return Deployments.createDeployment()
                .addClasses(MockServiceProducers.class);
    }

    private RequestSpecification configureEndpoint() {
        return new RequestSpecBuilder().setBaseUri(deploymentUri + "api/endpoint").build();
    }

    @Test
    public void should_return_skip_validation_for_any_token() {
        given()
                .spec(configureEndpoint())
        .when()
                .header("Authorization", "Bearer " + TOKEN_SIGNED_WITH_DIFFERENT_KEY)
                .get("/secured")
        .then()
                .assertThat().statusCode(200);
    }


}
