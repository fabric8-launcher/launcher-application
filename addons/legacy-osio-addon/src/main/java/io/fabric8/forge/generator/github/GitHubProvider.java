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
package io.fabric8.forge.generator.github;

import java.io.IOException;

import io.fabric8.forge.generator.git.GitAccount;
import io.fabric8.forge.generator.git.GitProvider;
import io.fabric8.forge.generator.git.WebHookDetails;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;

/**
 */
public class GitHubProvider extends GitProvider {
    private boolean registerWebHooks = true;

    public GitHubProvider() {
        super("github");
    }

    @Override
    public void addCreateRepositoryStep(NavigationResultBuilder builder) {
        builder.add(GitHubRepoStep.class);
    }

    @Override
    public void addConfigureStep(NavigationResultBuilder builder) {
    }

    @Override
    public void addImportRepositoriesSteps(NavigationResultBuilder builder) {
        builder.add(GitHubImportPickOrganisationStep.class);
        builder.add(GitHubImportPickRepositoriesStep.class);
    }

    @Override
    public void addGitCloneStep(NavigationResultBuilder builder) {
        builder.add(GitHubCloneStep.class);
    }

    @Override
    public boolean isConfiguredCorrectly() {
        return true;
    }

    @Override
    public void registerWebHook(GitAccount details, WebHookDetails webhook) throws IOException {
        if (registerWebHooks) {
            GitHubFacade facade = new GitHubFacade(details);
            facade.createWebHook(webhook);
        }
    }
}
