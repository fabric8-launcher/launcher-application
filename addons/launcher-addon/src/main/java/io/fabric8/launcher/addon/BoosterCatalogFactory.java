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

import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.furnace.container.cdi.events.Local;
import org.jboss.forge.furnace.event.PostStartup;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.openshift.booster.catalog.Booster;
import io.openshift.booster.catalog.BoosterCatalog;
import io.openshift.booster.catalog.BoosterCatalogService;
import io.openshift.booster.catalog.LauncherConfiguration;

/**
 * Factory class for {@link BoosterCatalogService} objects
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:tschotan@redhat.com">Tako Schotanus</a>
 */
@ApplicationScoped
public class BoosterCatalogFactory {

    public static final String LAUNCHER_SKIP_OOF_CATALOG_INDEX = "LAUNCHER_SKIP_OOF_CATALOG_INDEX";

    private static final String LAUNCHER_PREFETCH_BOOSTERS = "LAUNCHER_PREFETCH_BOOSTERS";

    private BoosterCatalog defaultBoosterCatalog;

    private Map<CatalogServiceKey, BoosterCatalogService> cache = new ConcurrentHashMap<>();

    @Resource
    private ManagedExecutorService async;

    @PostConstruct
    public void reset() {
        cache.clear();
        defaultBoosterCatalog = getCatalog(LauncherConfiguration.boosterCatalogRepositoryURI(), LauncherConfiguration.boosterCatalogRepositoryRef());
        // Index the openshift-online-free catalog
        if (!EnvironmentSupport.INSTANCE.getBooleanEnvVarOrSysProp(LAUNCHER_SKIP_OOF_CATALOG_INDEX)) {
            getCatalog(LauncherConfiguration.boosterCatalogRepositoryURI(), "openshift-online-free");
        }
    }

    public BoosterCatalog getCatalog(UIContext context) {
        Map<Object, Object> attributeMap = context.getAttributeMap();
        String catalogUrl = (String) attributeMap.get(LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REPOSITORY);
        String catalogRef = (String) attributeMap.get(LauncherConfiguration.PropertyName.LAUNCHER_BOOSTER_CATALOG_REF);
        if (catalogUrl == null && catalogRef == null) {
            return getDefaultCatalog();
        }
        return getCatalog(catalogUrl, catalogRef);
    }

    /**
     * @param catalogUrl the URL to use. Assumes {@link #DEFAULT_GIT_REPOSITORY_URL} if <code>null</code>
     * @param catalogRef the Git ref to use. Assumes {@link #DEFAULT_CATALOG_REF} if <code>null</code>
     * @return the {@link BoosterCatalogService} using the given catalog URL/ref tuple
     */
    public BoosterCatalog getCatalog(String catalogUrl, String catalogRef) {
        return cache.computeIfAbsent(
                new CatalogServiceKey(Objects.toString(catalogUrl, LauncherConfiguration.boosterCatalogRepositoryURI()),
                                      Objects.toString(catalogRef, LauncherConfiguration.boosterCatalogRepositoryRef())),
                key -> {
                    BoosterCatalogService service = new BoosterCatalogService.Builder()
                            .catalogRepository(key.getCatalogUrl())
                            .catalogRef(key.getCatalogRef())
                            .executor(async)
                            .build();
                    CompletableFuture<Set<Booster>> result = service.index();
                    if (EnvironmentSupport.INSTANCE.getBooleanEnvVarOrSysProp(LAUNCHER_PREFETCH_BOOSTERS, true)) {
                        result.thenRunAsync(service::prefetchBoosters);
                    }
                    return service;
                });
    }

    @Produces
    @Singleton
    public BoosterCatalog getDefaultCatalog() {
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
