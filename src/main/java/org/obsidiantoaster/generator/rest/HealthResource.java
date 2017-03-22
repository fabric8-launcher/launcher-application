package org.obsidiantoaster.generator.rest;

import java.io.StringReader;
import java.net.URI;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.obsidiantoaster.generator.util.JsonBuilder;

/**
 * Reports that the application is available to receive requests
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@Path(HealthResource.PATH_HEALTH)
@ApplicationScoped
public class HealthResource
{
   private static final String CATAPULT_SERVICE_URL = "CATAPULT_URL";
   private static final Logger log = Logger.getLogger(HealthResource.class.getName());

   public static final String PATH_HEALTH = "/health";
   public static final String PATH_READY = "/ready";
   public static final String PATH_CATAPULT_READY = "/catapult/ready";

   private static final String STATUS = "status";
   private static final String REASON = "reason";
   private static final String OK = "OK";
   private static final String ERROR = "ERROR";

   /**
    * Returns a JSON object with a single attribute, {@link HealthResource#STATUS}, with a value of
    * {@link HealthResource#OK} to show that we are ready to receive requests
    *
    * @return
    */
   @GET
   @Path(HealthResource.PATH_READY)
   @Produces(MediaType.APPLICATION_JSON)
   public JsonObject ready()
   {
      return Json.createObjectBuilder().add(STATUS, OK).build();
   }
   
   @GET
   @Path(HealthResource.PATH_CATAPULT_READY)
   @Produces(MediaType.APPLICATION_JSON)
   public JsonObject catapultReady() {
       Client client = ClientBuilder.newBuilder().build();
       try {
          WebTarget target = client.target(createCatapultUri());
          String json = target.request().get().readEntity(String.class);
          JsonObject object = Json.createReader(new StringReader(json)).readObject();
          return object;
       } catch (Exception ex) {
          return Json.createObjectBuilder().add(STATUS, ERROR).add(REASON, ex.getMessage()).build();
       } finally {
          client.close();
       }
   }

   private URI createCatapultUri() {
      String catapultUrlString = System.getProperty(CATAPULT_SERVICE_URL, System.getenv(CATAPULT_SERVICE_URL));
      if (catapultUrlString == null) {
         throw new WebApplicationException("'" + CATAPULT_SERVICE_URL + "' environment variable must be set!");
      }
      return UriBuilder.fromUri(catapultUrlString).path("/api/health/ready").build();
   }
}
