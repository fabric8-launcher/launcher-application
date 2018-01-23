/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon.catalog;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import io.openshift.booster.catalog.Booster;
import io.openshift.booster.catalog.BoosterCatalog;

/**
 * General operations for a set of {@link Booster} objects
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:tschotan@redhat.com">Tako Schotanus</a>
 */
public interface RhoarBoosterCatalog extends BoosterCatalog<RhoarBooster> {

    /**
     * @param mission The {@link Mission} belonging to the {@link Booster} object
     * @param runtime The {@link Runtime} belonging to the {@link Booster} object
     * @return an {@link Optional} for the given method parameters
     */
    Optional<RhoarBooster> getBooster(Mission mission, Runtime runtime);

    /**
     * @param mission The {@link Mission} belonging to the {@link Booster} object
     * @param runtime The {@link Runtime} belonging to the {@link Booster} object
     * @return an {@link Optional} for the given method parameters
     */
    Optional<RhoarBooster> getBooster(Mission mission, Runtime runtime, Version version);

    /**
     * @return an immutable {@link Set} of all {@link Mission} objects
     */
    Set<Mission> getMissions();

    /**
     * @param filter A {@link Predicate} used to filter the {@link Booster} objects
     * @return an immutable {@link Set} of filtered {@link Mission} objects
     */
    Set<Mission> getMissions(Predicate<RhoarBooster> filter);

    /**
     * @return an immutable {@link Set} of all {@link Runtime} objects
     */
    Set<Runtime> getRuntimes();

    /**
     * @param filter A {@link Predicate} used to filter the {@link Booster} objects
     * @return an immutable {@link Set} of filtered {@link Runtime} objects
     */
    Set<Runtime> getRuntimes(Predicate<RhoarBooster> filter);

    /**
     * @param filter A {@link Predicate} used to filter the {@link Booster} objects
     * @return an immutable {@link Set} of filtered {@link Version} objects
     */
    Set<Version> getVersions(Predicate<RhoarBooster> filter);

    /**
     * @param mission The {@link Mission} belonging to the {@link Version} objects
     * @param runtime The {@link Runtime} belonging to the {@link Version} objects
     * @return an immutable {@link Set} of filtered {@link Version} objects
     */
    Set<Version> getVersions(Mission mission, Runtime runtime);
}