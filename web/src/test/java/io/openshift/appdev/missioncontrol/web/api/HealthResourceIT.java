/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.openshift.appdev.missioncontrol.web.api;

import java.io.StringReader;
import java.net.URI;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
@RunWith(Arquillian.class)
public class HealthResourceIT
{
   @Deployment(testable = false)
   public static Archive<?> createDeployment()
   {
      return Deployments.createDeployment();
   }

   @ArquillianResource
   private URI deploymentUri;

   private Client client;
   private WebTarget readyTarget;

   @Before
   public void setup()
   {
      client = ClientBuilder.newClient();
      readyTarget = client.target(UriBuilder.fromUri(deploymentUri).path("health/ready"));
   }

   @Test
   public void readinessCheck()
   {
      final Response response = readyTarget.request().get();
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      String body = response.readEntity(String.class);
      assertNotNull(body);
      JsonObject entity = Json.createReader(new StringReader(body)).readObject();
      assertEquals("OK", entity.getString("status"));
      response.close();
   }
}
