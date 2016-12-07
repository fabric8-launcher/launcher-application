/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.obsidian.generator;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class Main
{
   public static void main(String[] args) throws Exception
   {
      Swarm swarm = new Swarm();
      swarm.fraction(UndertowFraction.createDefaultFraction("keystore.jks", "password", "appserver"));
      swarm.start().deploy();
   }
}
