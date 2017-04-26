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
package io.openshift.launchpad.backend.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 *
 */
@RunWith(Arquillian.class)
public class HealthResourceIT
{
   @Deployment
   public static Archive<?> createDeployment()
   {
      List<String> packageNames = Arrays.asList(LaunchResource.class.getPackage().getName().split("\\."));
      String packageName = packageNames.stream()
               .filter(input -> packageNames.indexOf(input) != packageNames.size() - 1)
               .collect(Collectors.joining("."));
      JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
      final File[] artifacts = Maven.resolver().loadPomFromFile("pom.xml")
               .resolve("org.jboss.forge:forge-service-core")
               .withTransitivity().asFile();
      deployment.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
      deployment.merge(ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class)
               .importDirectory("target/launchpad-backend/WEB-INF/addons").as(GenericArchive.class),
               "/WEB-INF/addons", Filters.include(".*"));
      deployment.addResource(LaunchResource.class);
      deployment.addResource(HealthResource.class);
      deployment.addPackages(true, packageName);
      deployment.addAsLibraries(artifacts);
      return deployment;
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
   @RunAsClient
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

   @Ignore("Until we can run the test against an actual Mission Control instance")
   @Test
   @RunAsClient
   public void catapultReadinessCheck() throws Exception
   {
      URI catapultServiceURI = HealthResource.createMissionControlUri();
      WebTarget catapultReadyTarget = client.target(catapultServiceURI);
      final Response response = catapultReadyTarget.request().get();
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      String body = response.readEntity(String.class);
      assertNotNull(body);
      JsonObject entity = Json.createReader(new StringReader(body)).readObject();
      assertEquals("OK", entity.getString("status"));
      response.close();
   }
}
