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
package io.openshift.launchpad.backend.web.api;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter
{
   public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
   public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
   public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
   public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
   public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
   public static final String ORIGIN = "Origin";
   public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
   public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
   public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

   protected boolean allowCredentials = true;
   protected String allowedMethods;
   protected String allowedHeaders;
   protected String exposedHeaders;
   protected int corsMaxAge = -1;
   protected Set<String> allowedOrigins = new HashSet<>();

   /**
    * Put "*" if you want to accept all origins
    *
    * @return
    */
   public Set<String> getAllowedOrigins()
   {
      return allowedOrigins;
   }

   /**
    * Defaults to true
    *
    * @return
    */
   public boolean isAllowCredentials()
   {
      return allowCredentials;
   }

   public void setAllowCredentials(boolean allowCredentials)
   {
      this.allowCredentials = allowCredentials;
   }

   /**
    * Will allow all by default
    *
    * @return
    */
   public String getAllowedMethods()
   {
      return allowedMethods;
   }

   /**
    * Will allow all by default comma delimited string for Access-Control-Allow-Methods
    *
    * @param allowedMethods
    */
   public void setAllowedMethods(String allowedMethods)
   {
      this.allowedMethods = allowedMethods;
   }

   public String getAllowedHeaders()
   {
      return allowedHeaders;
   }

   /**
    * Will allow all by default comma delimited string for Access-Control-Allow-Headers
    *
    * @param allowedHeaders
    */
   public void setAllowedHeaders(String allowedHeaders)
   {
      this.allowedHeaders = allowedHeaders;
   }

   public int getCorsMaxAge()
   {
      return corsMaxAge;
   }

   public void setCorsMaxAge(int corsMaxAge)
   {
      this.corsMaxAge = corsMaxAge;
   }

   public String getExposedHeaders()
   {
      return exposedHeaders;
   }

   /**
    * comma delimited list
    *
    * @param exposedHeaders
    */
   public void setExposedHeaders(String exposedHeaders)
   {
      this.exposedHeaders = exposedHeaders;
   }

   @Override
   public void filter(ContainerRequestContext requestContext) throws IOException
   {
      String origin = requestContext.getHeaderString(ORIGIN);
      if (origin == null)
      {
         return;
      }
      if (requestContext.getMethod().equalsIgnoreCase("OPTIONS"))
      {
         preflight(origin, requestContext);
      }
      else
      {
         checkOrigin(requestContext, origin);
      }
   }

   @Override
   public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException
   {
      String origin = requestContext.getHeaderString(ORIGIN);
      if (origin == null || requestContext.getMethod().equalsIgnoreCase("OPTIONS")
               || requestContext.getProperty("cors.failure") != null)
      {
         // don't do anything if origin is null, its an OPTIONS request, or cors.failure is set
         return;
      }
      responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
      if (allowCredentials)
         responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

      if (exposedHeaders != null)
      {
         responseContext.getHeaders().putSingle(ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeaders);
      }
   }

   protected void preflight(String origin, ContainerRequestContext requestContext) throws IOException
   {
      checkOrigin(requestContext, origin);

      Response.ResponseBuilder builder = Response.ok();
      builder.header(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
      if (allowCredentials)
         builder.header(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
      String requestMethods = requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_METHOD);
      if (requestMethods != null)
      {
         if (allowedMethods != null)
         {
            requestMethods = this.allowedMethods;
         }
         builder.header(ACCESS_CONTROL_ALLOW_METHODS, requestMethods);
      }
      String allowHeaders = requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_HEADERS);
      if (allowHeaders != null)
      {
         if (allowedHeaders != null)
         {
            allowHeaders = this.allowedHeaders;
         }
         builder.header(ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
      }
      if (corsMaxAge > -1)
      {
         builder.header(ACCESS_CONTROL_MAX_AGE, corsMaxAge);
      }
      requestContext.abortWith(builder.build());

   }

   protected void checkOrigin(ContainerRequestContext requestContext, String origin)
   {
      if (!allowedOrigins.contains("*") && !allowedOrigins.contains(origin))
      {
         requestContext.setProperty("cors.failure", true);
         throw new ForbiddenException("Origin not allowed: " + origin);
      }
   }
}