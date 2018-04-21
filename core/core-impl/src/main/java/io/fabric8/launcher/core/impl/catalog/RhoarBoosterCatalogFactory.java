/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.core.impl.catalog;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.booster.catalog.LauncherConfiguration;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalogService;
import io.fabric8.launcher.core.api.catalog.BoosterCatalogFactory;
import io.fabric8.utils.URLUtils;
import okhttp3.Request;

/**
 * Default implementation of BoosterCatalogFactory
 */
@ApplicationScoped
public class RhoarBoosterCatalogFactory implements BoosterCatalogFactory {

    private static final String LAUNCHER_BACKEND_ENVIRONMENT = "LAUNCHER_BACKEND_ENVIRONMENT";

    private static final String LAUNCHER_PREFETCH_BOOSTERS = "LAUNCHER_PREFETCH_BOOSTERS";

    private static final String LAUNCHER_BOOSTER_CATALOG_FILTER = "LAUNCHER_BOOSTER_CATALOG_FILTER";

    private static final String BOOSTER_ENVIRONMENT = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_BACKEND_ENVIRONMENT, defaultEnvironment());

    private static final boolean SHOULD_PREFETCH_BOOSTERS = EnvironmentSupport.INSTANCE.getBooleanEnvVarOrSysProp(LAUNCHER_PREFETCH_BOOSTERS, true);

    private static final String LAUNCHER_BOOSTER_CATALOG_FILTER_SCRIPT = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_BOOSTER_CATALOG_FILTER);

    private final AtomicReference<RhoarBoosterCatalogService> defaultBoosterCatalog = new AtomicReference<>();

    private static final Logger log = Logger.getLogger(RhoarBoosterCatalogFactory.class.getName());

    private final ExecutorService async;

    private final HttpClient httpClient;

    @Inject
    public RhoarBoosterCatalogFactory(ExecutorService async, HttpClient httpClient) {
        this.async = async;
        this.httpClient = httpClient;
    }

    /**
     * no-args constructor used by CDI for proxying only
     * but is subsequently replaced with an instance
     * created using the above constructor.
     *
     * @deprecated do not use this constructor
     */
    @Deprecated
    protected RhoarBoosterCatalogFactory() {
        this.async = null;
        this.httpClient = null;
    }

    // Initialize on startup
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        // Do nothing
    }

    @PostConstruct
    @Override
    public void reset() {
        defaultBoosterCatalog.set(null);
        getBoosterCatalog();
    }

    @Produces
    @Dependent
    @Override
    public RhoarBoosterCatalog getBoosterCatalog() {
        return defaultBoosterCatalog.updateAndGet(this::createBoosterCatalog);
    }

    @Override
    public void waitForIndex() throws InterruptedException, ExecutionException {
        RhoarBoosterCatalogService catalogService = defaultBoosterCatalog.get();
        if (catalogService != null) {
            catalogService.index().get();
        }
    }

    private RhoarBoosterCatalogService createBoosterCatalog(RhoarBoosterCatalogService current) {
        if (current != null) {
            return current;
        }
        RhoarBoosterCatalogService service = new RhoarBoosterCatalogService.Builder()
                .catalogRepository(LauncherConfiguration.boosterCatalogRepositoryURI())
                .catalogRef(resolveRef(LauncherConfiguration.boosterCatalogRepositoryURI(), LauncherConfiguration.boosterCatalogRepositoryRef()))
                .environment(BOOSTER_ENVIRONMENT)
                .filter(filter())
                .executor(async)
                .build();
        CompletableFuture<Set<RhoarBooster>> result = service.index();
        if (SHOULD_PREFETCH_BOOSTERS) {
            result.thenRunAsync(service::prefetchBoosters);
        }
        return service;
    }

    /**
     * @return
     */
    private static Predicate<RhoarBooster> filter() {
        Predicate<RhoarBooster> filter;
        if (LAUNCHER_BOOSTER_CATALOG_FILTER_SCRIPT != null) {
            filter = new ScriptingRhoarBoosterPredicate(LAUNCHER_BOOSTER_CATALOG_FILTER_SCRIPT);
        } else {
            filter = b -> true;
        }
        return filter;
    }

    // If no booster environment is specified we choose a default one ourselves:
    // we assume a "master" git ref means we're on development, otherwise "production"
    private static String defaultEnvironment() {
        if ("master".equals(LauncherConfiguration.boosterCatalogRepositoryRef())) {
            return "development";
        } else {
            return "production";
        }
    }

    /**
     * If gitRef == 'latest', then resolve the latest release from the repository
     *
     * https://api.github.com/repos/fabric8-launcher/launcher-booster-catalog/releases/latest
     */
    String resolveRef(String catalogUrl, String catalogRef) {
        if ("latest".equals(catalogRef)) {
            String url = catalogUrl.replace("https://github.com/", "https://api.github.com/repos/");
            if (url.endsWith(".git")) url = url.substring(0, url.lastIndexOf(".git"));
            String releaseUrl = URLUtils.pathJoin(url, "/releases/latest");
            log.fine(() -> "Querying release URL: " + releaseUrl);
            Request request = new Request.Builder().url(releaseUrl).build();
            String tagName = httpClient.executeAndParseJson(request, tree -> tree.get("tag_name").asText()).orElse(catalogRef);
            log.info(() -> "Resolving latest catalog tag to " + tagName);
            return tagName;
        }
        return catalogRef;
    }
}