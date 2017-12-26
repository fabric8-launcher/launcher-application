/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon;

import io.openshift.booster.catalog.BoosterCatalog;
import org.arquillian.smart.testing.rules.git.server.GitServer;
import io.openshift.booster.catalog.LauncherConfiguration;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class BoosterCatalogFactoryTest {

    private BoosterCatalogFactory factory;

    @ClassRule
    public static GitServer gitServer = GitServer.fromBundle("booster-catalog", "repos/boosters/booster-catalog.bundle")
       .usingPort(8765)
       .create();

    @Rule
    public final ProvideSystemProperty boosterCatalogProperties =
       new ProvideSystemProperty(LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REF, "openshift-online-free")
          .and("LAUNCHER_BOOSTER_CATALOG_REPOSITORY", "http://localhost:8765/booster-catalog");

    @Before
    public void setUp() {
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
        BoosterCatalog masterService = factory.getCatalog("http://localhost:8765/booster-catalog", "master");
        assertThat(masterService).isNotNull();
        assertThat(factory.getDefaultCatalog()).isNotSameAs(masterService);
    }

}
