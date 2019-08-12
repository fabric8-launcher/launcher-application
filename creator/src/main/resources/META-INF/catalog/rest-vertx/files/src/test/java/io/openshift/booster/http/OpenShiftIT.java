package io.openshift.booster.http;

import com.jayway.restassured.RestAssured;
import java.net.MalformedURLException;
import java.net.URL;
import org.arquillian.cube.openshift.impl.enricher.AwaitRoute;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.jayway.restassured.RestAssured.get;
import static io.openshift.booster.http.HttpApplication.TEMPLATE;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(Arquillian.class)
@org.junit.Ignore
public class OpenShiftIT {

    private final String applicationName = System.getProperty("app.name", "http-vertx");

    @RouteURL("${app.name}")
    @AwaitRoute
    private URL route;

    @Before
    public void setup() {
        RestAssured.baseURI = route.toString();
    }

    @Test
    public void testThatWeAreReady() throws Exception {
        // Check that the route is served.
        get().then().statusCode(equalTo(200));
        get("/api/greeting").then().statusCode(equalTo(200));
    }

    @Test
    public void testThatWeServeAsExpected() throws MalformedURLException {
        get("/api/greeting").then().body("content", equalTo(String.format(TEMPLATE, "World")));
        get("/api/greeting?name=vert.x").then().body("content", equalTo(String.format(TEMPLATE, "vert.x")));
    }
}
