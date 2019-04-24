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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.fabric8.launcher.base.Paths;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalogService;
import io.fabric8.launcher.core.api.catalog.BoosterCatalogFactory;
import io.quarkus.runtime.StartupEvent;
import okhttp3.Request;

import static io.fabric8.launcher.booster.catalog.LauncherConfiguration.boosterCatalogRepositoryRef;
import static io.fabric8.launcher.booster.catalog.LauncherConfiguration.boosterCatalogRepositoryURI;
import static io.fabric8.launcher.booster.catalog.rhoar.BoosterPredicates.withScriptFilter;
import static io.fabric8.launcher.core.impl.CoreEnvironment.LAUNCHER_BACKEND_ENVIRONMENT;
import static io.fabric8.launcher.core.impl.CoreEnvironment.LAUNCHER_BOOSTER_CATALOG_FILTER;
import static io.fabric8.launcher.core.impl.CoreEnvironment.LAUNCHER_FILTER_RUNTIME;
import static io.fabric8.launcher.core.impl.CoreEnvironment.LAUNCHER_FILTER_VERSION;
import static io.fabric8.launcher.core.impl.CoreEnvironment.LAUNCHER_PREFETCH_BOOSTERS;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Default implementation of BoosterCatalogFactory
 */
@ApplicationScoped
public class RhoarBoosterCatalogFactory implements BoosterCatalogFactory {

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
    public void init(@Observes StartupEvent init) {
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

    @Override
    public boolean isIndexing() {
        RhoarBoosterCatalogService catalogService = defaultBoosterCatalog.get();
        if (catalogService != null) {
            return !catalogService.index().isDone();
        }
        return false;
    }

    private RhoarBoosterCatalogService createBoosterCatalog(RhoarBoosterCatalogService current) {
        if (current != null) {
            return current;
        }
        RhoarBoosterCatalogService service = new RhoarBoosterCatalogService.Builder()
                .catalogRepository(boosterCatalogRepositoryURI())
                .catalogRef(resolveRef(boosterCatalogRepositoryURI(), boosterCatalogRepositoryRef()))
                .environment(LAUNCHER_BACKEND_ENVIRONMENT.value(defaultEnvironment()))
                .filter(filter())
                .executor(async)
                .build();
        CompletableFuture<Set<RhoarBooster>> result = service.index();
        if (LAUNCHER_PREFETCH_BOOSTERS.booleanValue(true)) {
            result.thenRunAsync(service::prefetchBoosters);
        }
        return service;
    }

    static Predicate<RhoarBooster> filter() {
        Predicate<RhoarBooster> filter = b -> true;
        String script = LAUNCHER_BOOSTER_CATALOG_FILTER.value();
        if (isNotBlank(script)) {
            filter = filter.and(withScriptFilter(script));
        }
        String allowedRuntimes = LAUNCHER_FILTER_RUNTIME.value();
        if (isNotBlank(allowedRuntimes)) {
            Predicate<RhoarBooster> runtimeFilter = b -> false;
            for (String allowedRuntime : allowedRuntimes.split(",")) {
                runtimeFilter = runtimeFilter.or(matches(BoosterPredicates::withRuntimeMatches, allowedRuntime.trim()));
            }
            filter = filter.and(runtimeFilter);
        }
        String allowedVersions = LAUNCHER_FILTER_VERSION.value();
        if (isNotBlank(allowedVersions)) {
            Predicate<RhoarBooster> versionFilter = b -> false;
            for (String allowedVersion : allowedVersions.split(",")) {
                versionFilter = versionFilter.or(matches(BoosterPredicates::withVersionMatches, allowedVersion.trim()));
            }
            filter = filter.and(versionFilter);
        }
        return filter;
    }

    private static Predicate<RhoarBooster> matches(Function<Pattern, Predicate<RhoarBooster>> function, String value) {
        return value.startsWith("!") ?
                function.apply(compile(value.substring(1))).negate() :
                function.apply(compile(value));
    }

    // If no booster environment is specified we choose a default one ourselves:
    // we assume a "master" git ref means we're on development, otherwise "production"
    private static String defaultEnvironment() {
        return ("master".equals(boosterCatalogRepositoryRef())) ? "development" : "production";
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
            String releaseUrl = Paths.join(url, "/releases/latest");
            log.fine(() -> "Querying release URL: " + releaseUrl);
            Request request = new Request.Builder().url(releaseUrl).build();
            String tagName = httpClient.executeAndParseJson(request, tree -> tree.get("tag_name").asText()).orElse(catalogRef);
            log.info(() -> "Resolving latest catalog tag to " + tagName);
            return tagName;
        }
        return catalogRef;
    }
}