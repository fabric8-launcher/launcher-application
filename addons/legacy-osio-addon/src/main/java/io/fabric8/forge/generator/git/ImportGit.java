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
package io.fabric8.forge.generator.git;

import io.fabric8.forge.generator.kubernetes.CreateBuildConfigStep;
import io.fabric8.forge.generator.pipeline.ChoosePipelineStep;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * Imports an organisation, selection of repos or all repos from a git provider
 */
public class ImportGit extends AbstractGitProviderCommand {
    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("fabric8: Import Git")
                .description("Imports a git organisation or repositories")
                .category(Categories.create("Fabric8"));
    }

    @Override
    protected void addNextStep(NavigationResultBuilder builder, GitProvider provider) {
        provider.addImportRepositoriesSteps(builder);
        provider.addGitCloneStep(builder);
        builder.add(ChoosePipelineStep.class);
        builder.add(GitCommitAndPushStep.class);
        builder.add(CreateBuildConfigStep.class);
    }

}
