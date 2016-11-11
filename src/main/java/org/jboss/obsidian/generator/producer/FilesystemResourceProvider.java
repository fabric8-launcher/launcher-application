/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.obsidian.generator.producer;

import java.nio.file.Path;

import javax.inject.Inject;

import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.service.spi.ResourceProvider;
import org.jboss.obsidian.generator.ForgeInitializer;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class FilesystemResourceProvider implements ResourceProvider
{

   @Inject
   private ResourceFactory resourceFactory;

   @Override
   public Resource<?> toResource(String path)
   {
      Path rootPath = ForgeInitializer.getRoot();
      return resourceFactory.create(rootPath.resolve(path).toFile());
   }

}
