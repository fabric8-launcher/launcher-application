/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.core.impl.documentation;

import java.nio.file.Path;

import org.junit.Test;

import static io.fabric8.launcher.core.impl.documentation.BoosterReadmePaths.getReadmePropertiesPath;
import static io.fabric8.launcher.core.impl.documentation.BoosterReadmePaths.getReadmeTemplatePath;
import static org.assertj.core.api.Assertions.assertThat;


public class BoosterDocumentationCloneRepoIT {

    @Test
    public void shouldCloneRepositoryCorrectly() {
        final Path path = BoosterDocumentationStoreImpl.cloneGitRepository();
        assertThat(getReadmePropertiesPath(path.toString(), "cd", "rest-http", "spring-boot"))
            .isRegularFile();
        assertThat(getReadmeTemplatePath(path.toString(), "rest-http"))
                .isRegularFile();
    }

}
