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
package io.fabric8.forge.generator.quickstart;

import java.util.Map;

import io.fabric8.forge.generator.github.GitHubImportRepoStep;
import io.fabric8.forge.generator.keycloak.ProfileSettings;
import io.fabric8.forge.generator.keycloak.ProfileSettingsDTO;
import io.fabric8.forge.generator.keycloak.TokenHelper;
import io.fabric8.forge.generator.kubernetes.CreateBuildConfigStep;
import io.fabric8.forge.generator.pipeline.ChoosePipelineStep;
import io.openshift.booster.catalog.DeploymentType;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lets add extra validation to the first page so that users can hit Finish early
 */
public class NewProjectWizard implements UIWizard {
    private static final transient Logger LOG = LoggerFactory.getLogger(NewProjectWizard.class);

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(this.getClass()).name("Fabric8: New Project").description("Generate your project from a booster").category(Categories.create(new String[]{"Openshift.io"}));
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        UIContext uiContext = builder.getUIContext();
        getProfileSettings(uiContext);
    }

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception {
        UIContext uiContext = context.getUIContext();
        ProfileSettings.updateAttributeMap(getProfileSettings(uiContext), uiContext);

        // default the deployment type
        Map<Object, Object> attributeMap = uiContext.getAttributeMap();
        if (!attributeMap.containsKey(DeploymentType.class)) {
            attributeMap.put(DeploymentType.class, DeploymentType.CD);
        }

        NavigationResultBuilder builder = NavigationResultBuilder.create();
        builder.add(ChooseBoosterStep.class);

        builder.add(Fabric8ProjectInfoStep.class);
        builder.add(ChoosePipelineStep.class);
        builder.add(GitHubImportRepoStep.class);
        builder.add(CreateBuildConfigStep.class);
        return builder.build();
    }

    private ProfileSettingsDTO getProfileSettings(UIContext uiContext) {
        ProfileSettingsDTO answer = (ProfileSettingsDTO) uiContext.getAttributeMap().get(ProfileSettingsDTO.class);
        if (answer == null) {
            String authHeader = TokenHelper.getAuthHeader(uiContext);
            answer = ProfileSettings.loadProfileSettings(authHeader);
            if (answer != null) {
                uiContext.getAttributeMap().put(ProfileSettingsDTO.class, answer);
            }
        }
        return answer;
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        return Results.success();
    }
}
