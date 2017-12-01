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

import java.util.List;

import javax.inject.Inject;

import io.fabric8.forge.generator.AttributeMapKeys;
import io.fabric8.forge.generator.cache.CacheFacade;
import io.fabric8.forge.generator.cache.CacheNames;
import io.fabric8.forge.generator.kubernetes.KubernetesClientFactory;
import io.fabric8.forge.generator.kubernetes.KubernetesClientHelper;
import org.infinispan.Cache;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.fabric8.forge.generator.git.GitProvider.pickDefaultGitProvider;

/**
 */
public abstract class AbstractPickGitAccountStep extends AbstractGitCommand implements UIWizardStep {
    final transient Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected Cache<String, List<GitProvider>> gitProviderCache;
    @Inject
    @WithAttributes(label = "git provider", required = true, description = "Select which git provider you wish to use")
    protected UISelectOne<GitProvider> gitProvider;
    @Inject
    private CacheFacade cacheManager;

    @Inject
    KubernetesClientFactory kubernetesClientFactory;

    public void initializeUI(final UIBuilder builder) throws Exception {
        super.initializeUI(builder);

        this.gitProviderCache = cacheManager.getCache(CacheNames.GIT_PROVIDERS);

        KubernetesClientHelper kubernetesClientHelper = kubernetesClientFactory.createKubernetesClient(builder.getUIContext());
        String key = kubernetesClientHelper.getUserCacheKey();
        List<GitProvider> gitServices = gitProviderCache.computeIfAbsent(key, k -> GitProvider.loadGitProviders());
        int size = gitServices.size();
        if (size > 0) {
            gitProvider.setDefaultValue(pickDefaultGitProvider(gitServices));
        }
        if (size > 1) {
            gitProvider.setValueChoices(gitServices);
            gitProvider.setItemLabelConverter(GitProvider::getName);
            builder.add(gitProvider);
        }
    }

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception {
        NavigationResultBuilder builder = NavigationResultBuilder.create();
        addNextSteps(builder);
        registerAttributes(context.getUIContext());
        return builder.build();
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        registerAttributes(context.getUIContext());
        return Results.success();
    }

    protected void registerAttributes(UIContext context) {
        GitProvider provider = gitProvider.getValue();
        if (provider != null) {
            context.getAttributeMap().put(AttributeMapKeys.GIT_PROVIDER, provider);
        }
    }

    protected abstract void addNextSteps(NavigationResultBuilder builder);

    protected GitProvider getMandatoryGitProvider() {
        GitProvider provider = gitProvider.getValue();
        if (provider == null) {
            throw new IllegalArgumentException("No git providers!");
        }
        return provider;
    }
}
