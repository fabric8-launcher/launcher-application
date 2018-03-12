package io.fabric8.launcher.core.impl.filters;

import java.net.URI;

import io.fabric8.launcher.core.api.security.Secured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

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
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(SecuredEndpoint.class, Secured.class, SecuredFilter.class)
                .addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml")
                                        .importCompileAndRuntimeDependencies().resolve().withTransitivity().asFile());
    }

    private RequestSpecification configureEndpoint() {
        return new RequestSpecBuilder().setBaseUri(deploymentUri + "endpoint").build();
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
    public void should_return_401_in_invalid_JWT_tokens() {
        given()
                .spec(configureEndpoint())
                .when()
                .header("Authorization", "Bearer xyz")
                .get("/secured")
                .then()
                .assertThat().statusCode(401);
    }


    @Test
    public void should_return_200_in_annotated_endpoints_with_token() {
        given()
                .spec(configureEndpoint())
                .when()
                .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE1MjA4NjQ3ODksImV4cCI6MTU1MjQwMDc4OSwiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsIkdpdmVuTmFtZSI6IkpvaG5ueSIsIlN1cm5hbWUiOiJSb2NrZXQiLCJFbWFpbCI6Impyb2NrZXRAZXhhbXBsZS5jb20iLCJSb2xlIjpbIk1hbmFnZXIiLCJQcm9qZWN0IEFkbWluaXN0cmF0b3IiXX0.CV5ETsUu_OVaks-qLexBFWf6a4syvLtn95L9TMctl3E")
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
