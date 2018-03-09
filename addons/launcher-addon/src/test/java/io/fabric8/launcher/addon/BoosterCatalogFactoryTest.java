/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon;

import io.fabric8.launcher.booster.catalog.BoosterCatalog;
import org.arquillian.smart.testing.rules.git.server.GitServer;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import static io.fabric8.launcher.booster.catalog.LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REF;
import static io.fabric8.launcher.booster.catalog.LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REPOSITORY;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class BoosterCatalogFactoryTest {

    private BoosterCatalogFactory factory;

    @ClassRule
    public static GitServer gitServer = GitServer.fromBundle("booster-catalog", "repos/boosters/booster-catalog.bundle")
            .usingAnyFreePort()
            .create();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public final ProvideSystemProperty boosterCatalogProperties =
            new ProvideSystemProperty(LAUNCHER_BOOSTER_CATALOG_REF, "openshift-online-free")
                    .and(LAUNCHER_BOOSTER_CATALOG_REPOSITORY, "http://localhost:" + gitServer.getPort() + "/booster-catalog");

    @Test
    public void testDefaultCatalogServiceNotNullAndIsSingleton() {
        factory = new BoosterCatalogFactory();
        // Forcing CDI initialization here
        factory.reset();

        BoosterCatalog defaultService = factory.getDefaultCatalog();
        softly.assertThat(defaultService).isNotNull();
        softly.assertThat(factory.getDefaultCatalog()).isSameAs(defaultService);
    }

    @Test
    public void testMasterCatalogIsNotSameAsDefault() {
        factory = new BoosterCatalogFactory();
        // Forcing CDI initialization here
        factory.reset();
        // A null catalogURL means use default repository URL
        BoosterCatalog masterService = factory.getCatalog(null, "master", null, false);
        softly.assertThat(masterService).isNotNull();
        softly.assertThat(factory.getDefaultCatalog()).isNotSameAs(masterService);
    }

    @Test
    public void testResolveRef() {
        String ref = BoosterCatalogFactory.resolveRef("https://github.com/fabric8-launcher/launcher-booster-catalog", "latest");
        softly.assertThat(ref).isNotEqualTo("latest");
    }

    @Test
    public void testResolveRefWithDotGit() {
        String ref = BoosterCatalogFactory.resolveRef("https://github.com/fabric8-launcher/launcher-booster-catalog.git", "latest");
        softly.assertThat(ref).isNotEqualTo("latest");
    }

}
