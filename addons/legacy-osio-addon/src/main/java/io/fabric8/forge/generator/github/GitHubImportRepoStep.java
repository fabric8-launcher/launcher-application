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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import io.fabric8.forge.generator.pipeline.AbstractDevToolsCommand;
import io.fabric8.project.support.UserDetails;
import io.fabric8.utils.Strings;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.fabric8.forge.generator.AttributeMapKeys.GIT_ACCOUNT;
import static io.fabric8.forge.generator.AttributeMapKeys.GIT_ORGANISATION;
import static io.fabric8.forge.generator.AttributeMapKeys.GIT_OWNER_NAME;
import static io.fabric8.forge.generator.AttributeMapKeys.GIT_PROVIDER;
import static io.fabric8.forge.generator.AttributeMapKeys.GIT_REPO_NAME;
import static io.fabric8.forge.generator.AttributeMapKeys.GIT_URL;
import static io.fabric8.forge.generator.git.AbstractGitRepoStep.importNewGitProject;
import static io.fabric8.forge.generator.git.AbstractGitRepoStep.updateGitURLInJenkinsfile;

/**
 */
public class GitHubImportRepoStep extends AbstractDevToolsCommand implements UIWizardStep {
    private static final transient Logger LOG = LoggerFactory.getLogger(GitHubImportRepoStep.class);

    /**
     * The name of the upstream repo
     */
    private String origin = "origin";

    /**
     * The default branch we make on creating repos
     */
    private String branch = "master";

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("Git Import Repo")
                .description("Import Git Repository")
                .category(Categories.create("Fabric8"));
    }


    @Override
    public void validate(UIValidationContext context) {
    }


    @Override
    public NavigationResult next(UINavigationContext context) throws Exception {
        return null;
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        UIContext uiContext = context.getUIContext();
        Map<Object, Object> attributeMap = uiContext.getAttributeMap();

        GitHubImportParameters importParameters = (GitHubImportParameters) attributeMap.get(GitHubImportParameters.class);
        if (importParameters == null) {
            return Results.fail("No GitHubImportParameters found!");
        }


        GitHubFacade github = importParameters.getGitHub();
        if (github == null) {
            return Results.fail("No github account setup");
        }
        String org = importParameters.getOrg();
        String repo = importParameters.getRepo();

        String orgAndRepo = importParameters.getOrgAndRepo();
        LOG.info("Creating github repository " + orgAndRepo);

        File basedir = getSelectionFolder(uiContext);
        if (basedir == null || !basedir.exists() || !basedir.isDirectory()) {
            return Results.fail("No project directory exists! " + basedir);
        }

        String gitOwnerName = org;
        String gitUrl = "https://github.com/" + orgAndRepo + ".git";
        String repoUrl = null;
        String gitHtmlUrl = null;
        try {
            //String gitRepoDescription = gitRepoDescription.getValue();
            String gitRepoDescription = "";
            GHRepository repository = github.createRepository(org, repo, gitRepoDescription);
            URL htmlUrl = repository.getHtmlUrl();
            if (htmlUrl != null) {
                gitHtmlUrl = htmlUrl.toString();
                repoUrl = gitHtmlUrl;
                gitUrl = repoUrl + ".git";
            }
            gitOwnerName = repository.getOwnerName();
        } catch (IOException e) {
            LOG.error("Failed to create repository  " + orgAndRepo + " " + e, e);
            return Results.fail("Failed to create repository  " + orgAndRepo + " " + e, e);
        }
        if (Strings.isNullOrBlank(repoUrl)) {
            repoUrl = gitUrl;
        }

        LOG.info("Created repository: " + repoUrl);
        attributeMap.put(GIT_URL, gitUrl);
        attributeMap.put(GIT_OWNER_NAME, gitOwnerName);
        attributeMap.put(GIT_ORGANISATION, org);
        attributeMap.put(GIT_REPO_NAME, repo);
        attributeMap.put(GIT_ACCOUNT, github.getDetails());
        attributeMap.put(GIT_PROVIDER, new GitHubProvider());

        Result result = updateGitURLInJenkinsfile(basedir, gitUrl, LOG);
        if (result != null) {
            return result;
        }

        try {
            UserDetails userDetails = github.createUserDetails(gitUrl);
            importNewGitProject(userDetails, basedir, "Initial import", gitUrl, branch, origin, LOG);
        } catch (Exception e) {
            LOG.error("Failed to import project to " + gitUrl + " " + e, e);
            return Results.fail("Failed to import project to " + gitUrl + ". " + e, e);
        }
        CreateGitRepoStatusDTO status = new CreateGitRepoStatusDTO(gitUrl, gitHtmlUrl, gitOwnerName, org, repo);
        return Results.success("Created git repository: " + repoUrl, status);
    }
}
