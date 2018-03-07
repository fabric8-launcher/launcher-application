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

import javax.inject.Inject;

import io.fabric8.forge.generator.cache.CacheFacade;
import io.fabric8.forge.generator.cache.CacheNames;
import io.fabric8.forge.generator.pipeline.AbstractDevToolsCommand;
import io.fabric8.utils.Strings;
import org.infinispan.Cache;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.input.UIInput;

/**
 */
public abstract class AbstractGitSetupCredentialsStep extends AbstractDevToolsCommand {
    protected Cache<String, GitAccount> accountCache;

    @Inject
    private CacheFacade cacheManager;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        super.initializeUI(builder);

        this.accountCache = cacheManager.getCache(CacheNames.GITHUB_ACCOUNT_FROM_SECRET);
    }

    protected void setIfNotBlank(UIInput<String> input, String value) {
        if (Strings.isNotBlank(value)) {
            input.setValue(value);
        }
    }
}
