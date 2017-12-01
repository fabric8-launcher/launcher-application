/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.forge.generator;

import io.fabric8.forge.generator.github.GitHubRepoStep;
import io.fabric8.forge.generator.kubernetes.CreateBuildConfigStep;
import io.fabric8.forge.generator.pipeline.ChoosePipelineStep;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;

/**
 */
public class CommonSteps {
    /**
     * Adds the steps to a wizard to pick a pipeline, create a github repository and create an OpenShift repository
     *
     * @param builder the wizards navigation builder
     */
    public static void addPipelineGitHubAndOpenShiftSteps(NavigationResultBuilder builder) {
        builder.add(ChoosePipelineStep.class);

        builder.add(GitHubRepoStep.class);
        builder.add(CreateBuildConfigStep.class);
    }
}
