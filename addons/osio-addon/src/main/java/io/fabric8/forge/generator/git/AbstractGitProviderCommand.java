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

import io.fabric8.forge.generator.AttributeMapKeys;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.wizard.UIWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static io.fabric8.forge.generator.git.GitProvider.pickDefaultGitProvider;

/**
 */
public abstract class AbstractGitProviderCommand implements UIWizard {
    private static final transient Logger LOG = LoggerFactory.getLogger(ConfigureGitAccount.class);
    private List<GitProvider> gitProviders;
    @Inject
    @WithAttributes(label = "git provider", required = true, description = "Select which git provider you wish to use")
    private UISelectOne<GitProvider> gitProvider;

    @Override
    public boolean isEnabled(UIContext context) {
        return true;
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        gitProviders = GitProvider.loadGitProviders();
        LOG.debug("Git providers: " + gitProviders);

        if (gitProviders.size() > 1) {
            builder.add(gitProvider);
        }
        gitProvider.setItemLabelConverter(GitProvider::getName);

        if (!gitProviders.isEmpty()) {
            gitProvider.setDefaultValue(pickDefaultGitProvider(this.gitProviders));
        }
    }

    @Override
    public void validate(UIValidationContext context) {
    }

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception {
        NavigationResultBuilder builder = NavigationResultBuilder.create();
        GitProvider provider = gitProvider.getValue();
        if (provider != null) {
            context.getUIContext().getAttributeMap().put(AttributeMapKeys.GIT_PROVIDER, provider);
            addNextStep(builder, provider);
        }
        return builder.build();
    }

    protected abstract void addNextStep(NavigationResultBuilder builder, GitProvider provider);

    @Override
    public Result execute(UIExecutionContext uiExecutionContext) throws Exception {
        return Results.success();
    }
}
