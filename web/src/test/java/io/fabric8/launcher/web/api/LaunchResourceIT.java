/**
 * Copyright 2005-2015 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.launcher.web.api;

import java.io.StringReader;
import java.net.URI;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import io.fabric8.launcher.web.forge.util.JsonBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
@RunWith(Arquillian.class)
public class LaunchResourceIT {
    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return Deployments.createDeployment();
    }

    @ArquillianResource
    private URI deploymentUri;

    private Client client;

    private WebTarget webTarget;

    @Before
    public void setup() {
        client = ClientBuilder.newClient();
        webTarget = client.target(UriBuilder.fromUri(deploymentUri).path("api").path("launchpad"));
    }

    @Test
    public void shouldRespondWithVersion() {
        final Response response = webTarget.path("/version").request().get();
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        response.close();
    }

    @Test
    public void shouldGoToNextStep() {
        final JsonObject jsonObject = new JsonBuilder().createJson(1)
                .addInput("deploymentType", "Continuous delivery")
                .build();

        final Response response = webTarget.path("/commands/launchpad-new-project/validate").request()
                .post(Entity.json(jsonObject.toString()));

        final String json = response.readEntity(String.class);
        // System.out.println(json);
        JsonObject object = Json.createReader(new StringReader(json)).readObject();
        assertNotNull(object);
        assertThat(object.toString()).contains("Mission must be specified.");
    }
}