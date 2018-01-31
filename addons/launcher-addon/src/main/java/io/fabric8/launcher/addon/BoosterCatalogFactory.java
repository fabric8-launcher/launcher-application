/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.booster.catalog.LauncherConfiguration;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalogService;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.furnace.container.cdi.events.Local;
import org.jboss.forge.furnace.event.PostStartup;

/**
 * Factory class for {@link BoosterCatalogService} objects
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:tschotan@redhat.com">Tako Schotanus</a>
 */
@ApplicationScoped
public class BoosterCatalogFactory {

    private static final String LAUNCHER_BACKEND_ENVIRONMENT = "LAUNCHER_BACKEND_ENVIRONMENT";

    private static final String LAUNCHER_PREFETCH_BOOSTERS = "LAUNCHER_PREFETCH_BOOSTERS";

    private static final String boosterEnvironment = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_BACKEND_ENVIRONMENT, defaultEnvironment());

    private static final boolean shouldPrefetchBoosters = EnvironmentSupport.INSTANCE.getBooleanEnvVarOrSysProp(LAUNCHER_PREFETCH_BOOSTERS, true);

    private RhoarBoosterCatalog defaultBoosterCatalog;

    private Map<CatalogServiceKey, RhoarBoosterCatalogService> cache = new ConcurrentHashMap<>();

    @Resource
    private ManagedExecutorService async;

    // If no booster environment is specified we choose a default one ourselves:
    // we assume a "master" git ref means we're on development, otherwise "production"
    private static String defaultEnvironment() {
        if ("master".equals(LauncherConfiguration.boosterCatalogRepositoryRef())) {
            return "development";
        } else {
            return "production";
        }
    }

    @PostConstruct
    public void reset() {
        cache.clear();
        defaultBoosterCatalog = getCatalog(LauncherConfiguration.boosterCatalogRepositoryURI(), LauncherConfiguration.boosterCatalogRepositoryRef(), boosterEnvironment, shouldPrefetchBoosters);
    }

    public RhoarBoosterCatalog getCatalog(UIContext context) {
        Map<Object, Object> attributeMap = context.getAttributeMap();
        String catalogUrl = (String) attributeMap.get(LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REPOSITORY);
        String catalogRef = (String) attributeMap.get(LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REF);
        if (catalogUrl == null && catalogRef == null) {
            return getDefaultCatalog();
        }
        return getCatalog(catalogUrl, catalogRef, boosterEnvironment, shouldPrefetchBoosters);
    }

    /**
     * @param catalogUrl the URL to use. Assumes {@link #DEFAULT_GIT_REPOSITORY_URL} if <code>null</code>
     * @param catalogRef the Git ref to use. Assumes {@link #DEFAULT_CATALOG_REF} if <code>null</code>
     * @return the {@link BoosterCatalogService} using the given catalog URL/ref tuple
     */
    public RhoarBoosterCatalog getCatalog(String catalogUrl, String catalogRef, String environment, boolean prefetchBoosters) {
        return cache.computeIfAbsent(
                new CatalogServiceKey(Objects.toString(catalogUrl, LauncherConfiguration.boosterCatalogRepositoryURI()),
                                      Objects.toString(catalogRef, LauncherConfiguration.boosterCatalogRepositoryRef())),
                key -> {
                    RhoarBoosterCatalogService service = new RhoarBoosterCatalogService.Builder()
                            .catalogRepository(key.getCatalogUrl())
                            .catalogRef(key.getCatalogRef())
                            .environment(environment)
                            .executor(async)
                            .build();
                    CompletableFuture<Set<RhoarBooster>> result = service.index();
                    if (prefetchBoosters) {
                        service.prefetchBoosters();
                    }
                    return service;
                });
    }

    @Produces
    @Singleton
    public RhoarBoosterCatalog getDefaultCatalog() {
        return defaultBoosterCatalog;
    }

    void init(@Observes @Local PostStartup startup) {
        // This will automatically call the reset method when constructed
    }

    private class CatalogServiceKey {
        /**
         * @param catalogUrl
         * @param catalogRef
         */
        public CatalogServiceKey(String catalogUrl, String catalogRef) {
            super();
            this.catalogUrl = catalogUrl;
            this.catalogRef = catalogRef;
        }

        private final String catalogUrl;

        private final String catalogRef;

        /**
         * @return the catalogRef
         */
        public String getCatalogRef() {
            return catalogRef;
        }

        /**
         * @return the catalogUrl
         */
        public String getCatalogUrl() {
            return catalogUrl;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CatalogServiceKey that = (CatalogServiceKey) o;
            return Objects.equals(catalogUrl, that.catalogUrl) &&
                    Objects.equals(catalogRef, that.catalogRef);
        }

        @Override
        public int hashCode() {
            return Objects.hash(catalogUrl, catalogRef);
        }
    }
}
