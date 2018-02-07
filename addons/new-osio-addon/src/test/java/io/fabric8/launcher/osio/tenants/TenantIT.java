package io.fabric8.launcher.osio.tenants;

import java.io.File;
import java.net.URI;

import io.fabric8.launcher.osio.EnvironmentVariables;
import io.fabric8.launcher.osio.HoverflyRuleConfigurer;
import io.fabric8.launcher.osio.http.ExternalRequest;
import io.fabric8.launcher.osio.tenant.Tenant;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.fabric8.launcher.osio.HoverflyRuleConfigurer.createHoverflyProxy;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE;
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME;
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */

@RunWith(Arquillian.class)
@RunAsClient
public class TenantIT {

    @ClassRule
    public static HoverflyRule witVirtualization = createHoverflyProxy("wit-simulation.json",
                                                                       "api.openshift.io|api.prod-preview.openshift.io", 8558);

    @ArquillianResource
    private URI deploymentUri;


    private RequestSpecification configureEndpoint() {
        return new RequestSpecBuilder()
                .addHeader("Authorization", "token")
                .setBaseUri(URI.create(deploymentUri + "osio/tenant")).build();
    }


    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importDependencies(RUNTIME, COMPILE, TEST)
                .resolve().withTransitivity().asFile();
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(new FileAsset(new File("src/main/resources/META-INF/beans.xml")), "beans.xml")
                .addPackages(true, Tenant.class.getPackage())
                .addPackages(true, ExternalRequest.class.getPackage())
                .addClasses(HoverflyRuleConfigurer.class, TenantTestEndpoint.class, EnvironmentVariables.class)
                .addAsLibraries(libs);
    }


    @Test
    public void readTenantData() {
        given().spec(configureEndpoint())
        .when()
                .get()
        .then()
                .assertThat().statusCode(200)
                .body("id", is("george"))
                .body("type", is("hoverfly"))
                .body("email", is("foo@example.com"));

    }

}
