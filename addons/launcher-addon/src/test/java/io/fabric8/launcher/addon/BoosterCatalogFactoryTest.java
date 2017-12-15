/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon;

import io.openshift.booster.catalog.BoosterCatalog;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class BoosterCatalogFactoryTest {

    private BoosterCatalogFactory factory;

    @Before
    public void setUp() {
        System.setProperty(BoosterCatalogFactory.LAUNCHER_CATALOG_REF, "openshift-online-free");
        factory = new BoosterCatalogFactory();
        // Forcing CDI initialization here
        factory.reset();
    }

    @Test
    public void testDefaultCatalogServiceNotNullAndIsSingleton() {
        BoosterCatalog defaultService = factory.getDefaultCatalog();
        assertThat(defaultService).isNotNull();
        assertThat(factory.getDefaultCatalog()).isSameAs(defaultService);
    }

    @Test
    public void testMasterCatalogIsNotSameAsDefault() {
        // A null catalogURL means use default repository URL
        BoosterCatalog masterService = factory.getCatalog(null, "master");
        assertThat(masterService).isNotNull();
        assertThat(factory.getDefaultCatalog()).isNotSameAs(masterService);
    }

}
