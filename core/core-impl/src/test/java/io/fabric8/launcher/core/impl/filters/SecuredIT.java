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

import static io.fabric8.launcher.base.test.identity.TokenFixtures.OUTDATED_TOKEN;
import static io.fabric8.launcher.base.test.identity.TokenFixtures.TOKEN_SIGNED_WITH_DIFFERENT_KEY;
import static io.fabric8.launcher.base.test.identity.TokenFixtures.VALID_TOKEN;
import static io.restassured.RestAssured.given;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SecuredIT {

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
    public void should_return_401_in_annotated_endpoints_without_token() {
        given()
                .spec(configureEndpoint())
        .when()
                .get("/secured")
        .then()
                .assertThat().statusCode(401);

    }

    @Test
    public void should_return_401_in_annotated_endpoints_with_invalid_authorization_header_token() {
        given()
                .spec(configureEndpoint())
        .when()
                .header("Authorization", "xyz")
                .get("/secured")
        .then()
                .assertThat().statusCode(401);

    }

    @Test
    public void should_return_401_when_token_is_outdated() {
        given()
                .spec(configureEndpoint())
        .when()
                .header("Authorization", "Bearer " + OUTDATED_TOKEN)
                .get("/secured")
        .then()
                .assertThat().statusCode(401);
    }

    @Test
    public void should_return_401_when_token_is_signed_by_different_key() {
        given()
                .spec(configureEndpoint())
        .when()
                .header("Authorization", "Bearer " + TOKEN_SIGNED_WITH_DIFFERENT_KEY)
                .get("/secured")
        .then()
                .assertThat().statusCode(401);
    }

    @Test
    public void should_return_200_in_annotated_endpoints_with_token() {
        given()
                .spec(configureEndpoint())
        .when()
                .header("Authorization", "Bearer " + VALID_TOKEN)
                .get("/secured")
        .then()
                .assertThat().statusCode(200);
    }

    @Test
    public void should_not_secure_not_annotated_endpoints() {
        given()
                .spec(configureEndpoint())
        .when()
                .get("/insecured")
        .then()
                .assertThat().statusCode(200);

    }

}
