/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.web.forge.cdi;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.jboss.forge.service.producer.FurnaceProducer;
import org.jboss.forge.service.producer.FurnaceServiceProducer;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class LauncherExtension implements Extension
{

   public void vetoFurnaceProducer(@Observes ProcessAnnotatedType<FurnaceProducer> furnaceProducer)
   {
      furnaceProducer.veto();
   }

   public void vetoFurnaceServiceProducer(@Observes ProcessAnnotatedType<FurnaceServiceProducer> furnaceServiceProducer)
   {
      furnaceServiceProducer.veto();
   }

}
