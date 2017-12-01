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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.fabric8.forge.generator.AttributeMapKeys;
import io.fabric8.utils.Strings;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class GitCloneStep extends AbstractGitRepoStep {
    final transient Logger LOG = LoggerFactory.getLogger(this.getClass());

    public GitCloneStep(String accountsCacheKey, String organisationsCacheKey) {
        super(accountsCacheKey, organisationsCacheKey);
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        UIContext uiContext = context.getUIContext();
        Map<Object, Object> attributeMap = uiContext.getAttributeMap();


        GitAccount details = (GitAccount) attributeMap.get(AttributeMapKeys.GIT_ACCOUNT);
        if (details == null) {
            return Results.fail("No attribute: " + AttributeMapKeys.GIT_ACCOUNT);
        }
        String gitOwnerName = (String) attributeMap.get(AttributeMapKeys.GIT_OWNER_NAME);
        if (Strings.isNullOrBlank(gitOwnerName)) {
            gitOwnerName = details.getUsername();
        }
        GitProvider gitProvider = (GitProvider) attributeMap.get(AttributeMapKeys.GIT_PROVIDER);
        if (gitProvider == null) {
            return Results.fail("No attribute: " + AttributeMapKeys.GIT_PROVIDER);
        }

        String gitRepoPattern = (String) uiContext.getAttributeMap().get(AttributeMapKeys.GIT_REPOSITORY_PATTERN);
        String gitRepoNameValue = (String) uiContext.getAttributeMap().get(AttributeMapKeys.GIT_REPO_NAME);
        Iterable<String> gitRepoNames = (Iterable<String>) uiContext.getAttributeMap().get(AttributeMapKeys.GIT_REPO_NAMES);
        if (Strings.isNullOrBlank(gitRepoNameValue) && gitRepoNames == null) {
            return Results.fail("No attribute: " + AttributeMapKeys.GIT_REPO_NAME + " or " + AttributeMapKeys.GIT_REPO_NAMES);
        }
        List<String> gitRepoNameList = new ArrayList<>();
        if (Strings.isNotBlank(gitRepoNameValue)) {
            gitRepoNameList.add(gitRepoNameValue);
        } else {
            for (String gitRepoName : gitRepoNames) {
                gitRepoNameList.add(gitRepoName);
            }
        }

        File tmpdir = Files.createTempDirectory("importdir").toFile();
        List<GitClonedRepoDetails> clonedRepos = new ArrayList<>();
        for (String gitRepoName : gitRepoNameList) {
            File basedir = new File(tmpdir, gitRepoName);
            CloneRepoAttributes attributes = createCloneRepoAttributes(gitOwnerName, gitRepoName, basedir);

            try {
                LOG.info("Cloning repository " + attributes.getUri() + " into directory " + attributes.getRemote());
                Git git = gitProvider.cloneRepo(attributes);

                GitClonedRepoDetails clonedRepo = new GitClonedRepoDetails(gitRepoName, git, attributes);
                clonedRepos.add(clonedRepo);
            } catch (GitAPIException e) {
                removeTemporaryFiles(basedir);
                return Results.fail("Failed to clone repository " + attributes.getUri() + " due to " + e, e);
            }
        }
        attributeMap.put(AttributeMapKeys.GIT_CLONED_REPOS, clonedRepos);
        return Results.success();
    }

    protected abstract CloneRepoAttributes createCloneRepoAttributes(String gitOwnerName, String gitRepoName, File dir);

}
