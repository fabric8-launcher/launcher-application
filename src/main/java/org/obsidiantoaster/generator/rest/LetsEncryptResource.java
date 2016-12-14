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
package org.obsidiantoaster.generator.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Needed to generate a Let's Encrypt certificate
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/.well-known/acme-challenge/idOih37COMAdJAT-fJtuBAGru9XLWPnxuXtKGzm7C40")
public class LetsEncryptResource
{
   @GET
   @Produces(MediaType.TEXT_PLAIN)
   public String getContents()
   {
      return "idOih37COMAdJAT-fJtuBAGru9XLWPnxuXtKGzm7C40.n6UzmiWpkLSV9JMaOOzZSZXZuBhSBF5YPa_vpEvgX_0";
   }
}
