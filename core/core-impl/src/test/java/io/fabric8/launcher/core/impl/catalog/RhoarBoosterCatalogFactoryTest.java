/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.core.impl.catalog;

import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.impl.CoreEnvironment;
import org.arquillian.smart.testing.rules.git.server.GitServer;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import static io.fabric8.launcher.booster.catalog.LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REF;
import static io.fabric8.launcher.booster.catalog.LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REPOSITORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@EnableRuleMigrationSupport
public class RhoarBoosterCatalogFactoryTest {

    @ClassRule
    public static GitServer gitServer = GitServer.fromBundle("booster-catalog", "repos/boosters/booster-catalog.bundle")
            .usingAnyFreePort()
            .create();

    @Rule
    public final ProvideSystemProperty boosterCatalogProperties =
            new ProvideSystemProperty(LAUNCHER_BOOSTER_CATALOG_REF, "openshift-online-free")
                    .and(LAUNCHER_BOOSTER_CATALOG_REPOSITORY, "http://localhost:" + gitServer.getPort() + "/booster-catalog");

    private RhoarBoosterCatalogFactory factory;

    @BeforeEach
    void setUp() {
        factory = new RhoarBoosterCatalogFactory(ForkJoinPool.commonPool(), HttpClient.create());
    }

    @Test
    void testDefaultCatalogServiceNotNullAndIsSingleton() {
        RhoarBoosterCatalog defaultService = factory.getBoosterCatalog();
        assertThat(defaultService).isNotNull();
        assertThat(factory.getBoosterCatalog()).isSameAs(defaultService);
    }

    @Test
    void testResolveRef() {
        String ref = factory.resolveRef("https://github.com/fabric8-launcher/launcher-booster-catalog", "latest");
        assertThat(ref).isNotEqualTo("latest");
    }

    @Test
    void testResolveRefWithDotGit() {
        String ref = factory.resolveRef("https://github.com/fabric8-launcher/launcher-booster-catalog.git", "latest");
        assertThat(ref).isNotEqualTo("latest");
    }

    @Test
    void testFilterRuntime() {
        System.setProperty(CoreEnvironment.LAUNCHER_FILTER_RUNTIME.propertyKey(), "!vert.x");
        final Predicate<RhoarBooster> filter = RhoarBoosterCatalogFactory.filter();
        final RhoarBooster mock = mock(RhoarBooster.class);
        when(mock.getRuntime()).thenReturn(new Runtime("vert.x"));
        assertThat(filter).rejects(mock);
        when(mock.getRuntime()).thenReturn(new Runtime("spring-boot"));
        assertThat(filter).accepts(mock);
        when(mock.getRuntime()).thenReturn(new Runtime("thorntail"));
        assertThat(filter).accepts(mock);
    }

    @Test
    void testFilterVersion() {
        System.setProperty(CoreEnvironment.LAUNCHER_FILTER_VERSION.propertyKey(), "redhat-.*");
        final Predicate<RhoarBooster> filter = RhoarBoosterCatalogFactory.filter();
        final RhoarBooster mock = mock(RhoarBooster.class);
        when(mock.getVersion()).thenReturn(new Version("redhat-current"));
        assertThat(filter).accepts(mock);
        when(mock.getVersion()).thenReturn(new Version("community-current"));
        assertThat(filter).rejects(mock);
        when(mock.getVersion()).thenReturn(new Version("community-redhat-current"));
        assertThat(filter).rejects(mock);
    }

}
