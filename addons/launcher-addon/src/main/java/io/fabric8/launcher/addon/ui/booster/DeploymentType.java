/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon.ui.booster;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Deprecated
public enum DeploymentType {
    /**
     * Deploy in Openshift
     */
    CD("Continuous delivery"),
    /**
     * Deploy as a ZIP file
     */
    ZIP("ZIP File");

    DeploymentType(String description) {
        this.description = description;
    }

    private final String description;

    public String getDescription() {
        return description;
    }
}
