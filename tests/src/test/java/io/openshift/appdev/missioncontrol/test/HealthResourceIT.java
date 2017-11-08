package io.openshift.appdev.missioncontrol.test;

import java.io.StringReader;
import java.net.URL;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;

import io.openshift.appdev.missioncontrol.web.api.HealthResource;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Validation of the {@link HealthResource}
 */
@RunWith(Arquillian.class)
@RunAsClient
public class HealthResourceIT {

    private static final Logger log = Logger.getLogger(HealthResourceIT.class.getName());

    /*
     Contracts (define here; do NOT link back to where these are defined in runtime code;
     if the runtime code changes that's a contract break)
     */
    private static final String PATH_READY = "/api/health/ready";

    @ArquillianResource
    private URL deploymentUrl;

    /**
     * Deploy the WAR as built since we only test via the API endpoints
     *
     * @return
     */
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getMavenBuiltWar();
    }

    @Test
    public void readinessCheck() throws Exception {
        final URL requestURL = new URL(deploymentUrl, PATH_READY);
        final Response response = new OkHttpClient().newCall(
                new Request.Builder().url(requestURL).build()).execute();
        final String body = response.body().string();
        log.info("Response from " + requestURL.toExternalForm() + ": " + body);
        final JsonObject json = Json.createReader(new StringReader(body)).readObject();
        Assert.assertEquals("JSON status message was not as expected", "OK", json.getString("status"));
        Assert.assertEquals("Response code was not as expected", 200, response.code());
    }


}