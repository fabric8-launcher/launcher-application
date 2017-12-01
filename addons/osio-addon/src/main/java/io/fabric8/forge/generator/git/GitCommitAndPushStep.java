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

import java.io.File;
import java.util.List;
import java.util.Map;

import io.fabric8.forge.generator.AttributeMapKeys;
import io.fabric8.forge.generator.cache.CacheNames;
import io.fabric8.project.support.UserDetails;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a git add, commit and push on the given git repos
 */
public class GitCommitAndPushStep extends AbstractGitRepoStep {
    final transient Logger LOG = LoggerFactory.getLogger(this.getClass());

    public GitCommitAndPushStep() {
        super(CacheNames.GITHUB_ACCOUNT_FROM_SECRET, CacheNames.GITHUB_ORGANISATIONS);
    }

    public void initializeUI(final UIBuilder builder) throws Exception {
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        UIContext uiContext = context.getUIContext();
        Map<Object, Object> attributeMap = uiContext.getAttributeMap();

        List<GitClonedRepoDetails> clonedRepos = (List<GitClonedRepoDetails>) attributeMap.get(AttributeMapKeys.GIT_CLONED_REPOS);
        if (clonedRepos != null) {
            for (GitClonedRepoDetails clonedRepo : clonedRepos) {
                Git git = clonedRepo.getGit();
                String gitUrl = clonedRepo.getGitUrl();
                UserDetails userDetails = clonedRepo.getUserDetails();
                File basedir = clonedRepo.getDirectory();
                String message = "Adding pipeline";
                try {
                    LOG.info("Performing a git commit and push on URI " + gitUrl);
                    gitAddCommitAndPush(git, gitUrl, userDetails, basedir, message);
                } catch (GitAPIException e) {
                    return Results.fail("Failed to commit and push repository " + clonedRepo.getGitRepoName() + " due to " + e, e);
                } finally {
                    removeTemporaryFiles(basedir);
                }
            }
        }
        return Results.success();
    }

}
