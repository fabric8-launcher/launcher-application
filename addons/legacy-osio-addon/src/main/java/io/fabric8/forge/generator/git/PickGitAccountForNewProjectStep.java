/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.forge.generator.git;

import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;

/**
 * When running on premise lets let the user setup their github credentials and store them in a Secret
 */
public class PickGitAccountForNewProjectStep extends AbstractPickGitAccountStep {

    @Override
    protected void addNextSteps(NavigationResultBuilder builder) {
        GitProvider provider = getMandatoryGitProvider();
        provider.addCreateRepositoryStep(builder);
    }
}
