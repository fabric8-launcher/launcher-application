package io.fabric8.launcher.core.impl.documentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.fabric8.launcher.core.api.documentation.BoosterDocumentationStore;

import static java.util.Objects.requireNonNull;

@ApplicationScoped
public final class BoosterDocumentationStoreImpl implements BoosterDocumentationStore {

    private static final Logger logger = Logger.getLogger(BoosterDocumentationStoreImpl.class.getName());

    private static final String DOCUMENTATION_REPOSITORY = "https://github.com/fabric8-launcher/launcher-documentation.git";

    private static final String DOCUMENTATION_BRANCH = "master";

    private volatile CompletableFuture<Path> pathCompletableFuture;

    private final ExecutorService executorService;

    private Supplier<Path> documentationPathSupplier;

    @Inject
    public BoosterDocumentationStoreImpl(final ExecutorService executorService) {
        this(executorService, BoosterDocumentationStoreImpl::cloneGitRepository);
    }

    //Visible for testing
    BoosterDocumentationStoreImpl(final ExecutorService executorService, final Supplier<Path> documentationPathSupplier) {
        this.executorService = requireNonNull(executorService, "executorService must be specified.");
        this.documentationPathSupplier = requireNonNull(documentationPathSupplier, "documentationPathSupplier must be specified.");
    }

    // Initialize on startup
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        // Do nothing
    }

    @PostConstruct
    public void initialize() {
        getDocumentationPath();
    }

    @Override
    public CompletableFuture<Path> reloadDocumentation() {
        return getDocumentationPath(true);
    }

    @Override
    public synchronized CompletableFuture<Path> getDocumentationPath() {
        return getDocumentationPath(false);
    }

    @Override
    public void waitForDocumentation() throws ExecutionException, InterruptedException {
        getDocumentationPath().get();
    }

    private synchronized CompletableFuture<Path> getDocumentationPath(final boolean reload) {
        if (reload || pathCompletableFuture == null) {
            pathCompletableFuture = createDocumentationPathFuture();
        }
        return pathCompletableFuture;
    }

    private CompletableFuture<Path> createDocumentationPathFuture() {
        return CompletableFuture.supplyAsync(() -> documentationPathSupplier.get(), executorService);
    }

    //Visible for testing
    static Path cloneGitRepository() {
        final String readmeRepositoryURI = DOCUMENTATION_REPOSITORY;
        final String branch = DOCUMENTATION_BRANCH;
        logger.log(Level.INFO, "Indexing contents from {0} using {1} ref",
                   new Object[]{readmeRepositoryURI, branch});

        try {
            final Path catalogPath = Files.createTempDirectory("booster-documentation");
            logger.info("Created " + catalogPath);
            final ProcessBuilder builder = new ProcessBuilder()
                    .command("git", "clone", readmeRepositoryURI,
                             "--branch", branch,
                             "--recursive",
                             "--depth=1",
                             "--quiet",
                             "-c", "advice.detachedHead=false",
                             catalogPath.toString())
                    .inheritIO();
            logger.info("Executing: " + builder.command().stream().collect(Collectors.joining(" ")));
            final int exitCode = builder.start().waitFor();
            assert exitCode == 0 : "Process returned exit code: " + exitCode;
            return catalogPath;
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Error while loading documentation", e);
            throw new IllegalStateException("Error while loading documentation", e);
        } catch (final InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted while loading documentation");
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while loading documentation", e);
        }
    }


}
