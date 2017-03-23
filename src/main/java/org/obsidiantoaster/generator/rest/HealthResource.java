package org.obsidiantoaster.generator.rest;

import java.io.StringReader;
import java.net.URI;

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
import javax.ws.rs.core.UriBuilder;

/**
 * Reports that the application is available to receive requests
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@Path(HealthResource.PATH_HEALTH)
@ApplicationScoped
public class HealthResource
{
   private static final String CATAPULT_SERVICE_HOST = "CATAPULT_SERVICE_HOST";
   private static final String CATAPULT_SERVICE_PORT = "CATAPULT_SERVICE_PORT";

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
   public JsonObject catapultReady()
   {
      Client client = ClientBuilder.newBuilder().build();
      try
      {
         WebTarget target = client.target(createCatapultUri());
         String json = target.request().get().readEntity(String.class);
         JsonObject object = Json.createReader(new StringReader(json)).readObject();
         return object;
      }
      catch (Exception ex)
      {
         return Json.createObjectBuilder().add(STATUS, ERROR).add(REASON, ex.getMessage()).build();
      }
      finally
      {
         client.close();
      }
   }

   private URI createCatapultUri()
   {
      String host = System.getProperty(CATAPULT_SERVICE_HOST, System.getenv(CATAPULT_SERVICE_HOST));
      if (host == null)
      {
         throw new WebApplicationException("'" + CATAPULT_SERVICE_HOST + "' environment variable must be set!");
      }
      UriBuilder uri = UriBuilder.fromPath("/api/health/ready").host(host).scheme("http");
      String port = System.getProperty(CATAPULT_SERVICE_PORT, System.getenv(CATAPULT_SERVICE_PORT));
      uri.port(port != null ? Integer.parseInt(port) : 80);
      return uri.build();
   }
}
