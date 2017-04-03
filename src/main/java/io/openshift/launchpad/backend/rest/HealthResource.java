package io.openshift.launchpad.backend.rest;

import java.io.StringReader;
import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
   private static final String LAUNCHPAD_MISSIONCONTROL_SERVICE_HOST = "LAUNCHPAD_MISSIONCONTROL_SERVICE_HOST";
   private static final String LAUNCHPAD_MISSIONCONTROL_SERVICE_PORT = "LAUNCHPAD_MISSIONCONTROL_SERVICE_PORT";

   public static final String PATH_HEALTH = "/health";
   public static final String PATH_READY = "/ready";
   public static final String PATH_MISSIONCONTROL = "/missioncontrol";

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
   @Path(PATH_READY)
   @Produces(MediaType.APPLICATION_JSON)
   public JsonObject ready()
   {
      return Json.createObjectBuilder().add(STATUS, OK).build();
   }

   @GET
   @Path(PATH_MISSIONCONTROL + PATH_READY)
   @Produces(MediaType.APPLICATION_JSON)
   public JsonObject catapultReady()
   {
      Client client = ClientBuilder.newBuilder().build();
      try
      {
         WebTarget target = client.target(createMissionControlUri());
         String json = target.request().get().readEntity(String.class);
         JsonObject object = Json.createReader(new StringReader(json)).readObject();
         return object;
      }
      catch (Exception ex)
      {
         String message = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
         return Json.createObjectBuilder().add(STATUS, ERROR).add(REASON, message).build();
      }
      finally
      {
         client.close();
      }
   }

   public static URI createMissionControlUri()
   {
      String host = System.getProperty(LAUNCHPAD_MISSIONCONTROL_SERVICE_HOST,
               System.getenv(LAUNCHPAD_MISSIONCONTROL_SERVICE_HOST));
      if (host == null)
      {
         host = "mission-control";
      }
      UriBuilder uri = UriBuilder.fromPath("/api/health/ready").host(host).scheme("http");
      String port = System.getProperty(LAUNCHPAD_MISSIONCONTROL_SERVICE_PORT,
               System.getenv(LAUNCHPAD_MISSIONCONTROL_SERVICE_PORT));
      uri.port(port != null ? Integer.parseInt(port) : 80);
      return uri.build();
   }
}
