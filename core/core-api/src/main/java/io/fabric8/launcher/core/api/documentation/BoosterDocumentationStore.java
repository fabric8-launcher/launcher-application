package io.fabric8.launcher.core.api.documentation;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * File store and (re)loader for booster documentation
 */
public interface BoosterDocumentationStore {
    /**
     * Reload the documentation for the source and get the documentation project path as a future
     *
     * @return the {@link CompletableFuture<Path>}
     */
    CompletableFuture<Path> reloadDocumentation();

    /**
     * Get the documentation project path as a future
     *
     * @return the {@link CompletableFuture<Path>}
     */
    CompletableFuture<Path> getDocumentationPath();

    /**
     * Wait until the current documentation store is loaded
     */
    void waitForDocumentation() throws ExecutionException, InterruptedException;
}
