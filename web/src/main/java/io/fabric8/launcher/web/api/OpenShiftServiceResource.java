/**
 * Copyright 2005-2015 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.launcher.web.api;

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.utils.URLUtils;
import io.fabric8.utils.Strings;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@javax.ws.rs.Path("/services")
@ApplicationScoped
public class OpenShiftServiceResource
{
   private static final String OPENSHIFT_API_URL = "OPENSHIFT_API_URL";
   private static final Logger log = Logger.getLogger(OpenShiftServiceResource.class.getName());

   @GET
   @javax.ws.rs.Path("/jenkins/{namespace}/{path: .*}")
   public Response jenkins(
            @PathParam("namespace") String namespace,
            @PathParam("path") String path,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo)
            throws Exception
   {
      String serviceName = "jenkins";
      return proxyRequest(namespace, path, headers, uriInfo, serviceName, "GET", null);

   }

   @POST
   @javax.ws.rs.Path("/jenkins/{namespace}/{path: .*}")
   public Response jenkinsPost(
            @PathParam("namespace") String namespace,
            @PathParam("path") String path,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo,
            String body)
            throws Exception
   {
      String serviceName = "jenkins";
      return proxyRequest(namespace, path, headers, uriInfo, serviceName, "POST", body);
   }

   private Response proxyRequest(String namespace, String path,
            HttpHeaders headers, UriInfo uriInfo, String serviceName, String method, String body)
   {
      String authorization = headers.getHeaderString("Authorization");
      if (Strings.isNullOrBlank(authorization))
      {
         return Response.status(Status.UNAUTHORIZED).build();
      }
      String token = authorization;
      int idx = token.indexOf(' ');
      if (idx >= 0)
      {
         token = token.substring(idx + 1);
      }
      if (Strings.isNullOrBlank(token))
      {
         return Response.status(Status.UNAUTHORIZED).entity("Empty token").build();
      }

      String openShiftApiUrl = System.getenv(OPENSHIFT_API_URL);
      if (Strings.isNullOrBlank(openShiftApiUrl))
      {
         return Response.serverError().entity("No environment variable defined: "
                  + OPENSHIFT_API_URL + " so cannot connect to OpenShift Online!").build();
      }
      Config config = new ConfigBuilder().withMasterUrl(openShiftApiUrl).withOauthToken(token).
               // TODO until we figure out the trust thing lets ignore warnings
                        withTrustCerts(true).
                        build();
      KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);

      String serviceUrl = KubernetesHelper.getServiceURL(kubernetesClient, serviceName, namespace, "https", true);
      if (Strings.isNullOrBlank(serviceUrl))
      {
         return Response.status(Status.NOT_FOUND)
                  .entity("Could not find service " + serviceName + " in namespace + " + namespace).build();
      }

      String query = uriInfo.getRequestUri().getQuery();
      String fullUrl = URLUtils.pathJoin(serviceUrl, path);
      if (!Strings.isNullOrBlank(query))
      {
         fullUrl += "?" + query;
      }

      log.info("Invoking " + method + " on " + fullUrl);

      HttpURLConnection connection = null;
      try
      {
         URL url = new URL(fullUrl);
         connection = (HttpURLConnection) url.openConnection();
         connection.setRequestMethod(method);
         MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
         for (Map.Entry<String, List<String>> entry : requestHeaders.entrySet())
         {
            String headerName = entry.getKey();
            List<String> values = entry.getValue();
            if (values != null)
            {
               for (String value : values)
               {
                  connection.setRequestProperty(headerName, value);
               }
            }
         }
         if (body != null)
         {
            connection.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(
                     connection.getOutputStream());
            out.write(body);

            out.close();
         }
         int status = connection.getResponseCode();
         String message = connection.getResponseMessage();
         log.info("Got response code from : " + status + " message: " + message);
         return Response.status(status).entity(connection.getInputStream()).build();
      }
      catch (Exception e)
      {
         log.log(Level.SEVERE, "Failed to invoke url " + fullUrl + ". " + e, e);
         return Response.serverError().entity("Failed to invoke " + fullUrl + " due to " + e).build();

      }
      finally
      {
         if (connection != null)
         {
            connection.disconnect();
         }
      }
   }

}
