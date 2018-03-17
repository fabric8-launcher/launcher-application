/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.core.api.catalog;

import java.util.concurrent.ExecutionException;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalog;

/**
 * Factory class for {@link RhoarBoosterCatalog} objects
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:tschotan@redhat.com">Tako Schotanus</a>
 */
public interface BoosterCatalogFactory {

    void reset();

    RhoarBoosterCatalog getBoosterCatalog();

    /**
     * Waits until the index operation is finished (Used in integration tests)
     */
    void waitForIndex() throws InterruptedException, ExecutionException;

}