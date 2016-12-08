/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.obsidian.generator.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Needed to generate a Let's Encrypt certificate
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path("/.well-known/acme-challenge/idOih37COMAdJAT-fJtuBAGru9XLWPnxuXtKGzm7C40")
public class LetsEncryptResource
{
   @GET
   public String getContents()
   {
      return "idOih37COMAdJAT-fJtuBAGru9XLWPnxuXtKGzm7C40.n6UzmiWpkLSV9JMaOOzZSZXZuBhSBF5YPa_vpEvgX_0";
   }
}
