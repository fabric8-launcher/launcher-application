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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import io.fabric8.forge.generator.cache.CacheFacade;
import io.fabric8.forge.generator.cache.CacheNames;
import io.fabric8.forge.generator.git.GitOrganisationDTO;
import io.fabric8.forge.generator.github.GitHubFacade;
import io.fabric8.forge.generator.github.GitHubFacadeFactory;
import io.fabric8.forge.generator.github.GitHubImportParameters;
import io.fabric8.launcher.addon.ui.booster.ProjectInfoStep;
import io.fabric8.utils.Strings;
import io.openshift.booster.catalog.DeploymentType;
import org.infinispan.Cache;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.fabric8.forge.generator.git.AbstractGitRepoStep.getOrganisationName;
import static io.fabric8.forge.generator.pipeline.AbstractDevToolsCommand.getSelectionFolder;

/**
 */
@Typed(Fabric8ProjectInfoStep.class)
public class Fabric8ProjectInfoStep extends ProjectInfoStep {
    private static final transient Logger LOG = LoggerFactory.getLogger(NewProjectWizard.class);

    @Inject
    @WithAttributes(label = "Organization", required = true, description = "The github organization to create this project inside")
    private UISelectOne<GitOrganisationDTO> gitOrganisation;

    @Inject
    protected CacheFacade cacheManager;

    protected Cache<String, Collection<GitOrganisationDTO>> organisationsCache;
    private Collection<GitOrganisationDTO> organisations = new ArrayList<>();

    /**
     * The name of the upstream repo
     */
    private String origin = "origin";
    /**
     * The default branch we make on creating repos
     */
    private String branch = "master";

    private GitHubFacade github;

    @Inject
    private GitHubFacadeFactory gitHubFacadeFactory;

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("Fabric8: Project Info")
                .description("Project Information")
                .category(Categories.create("Fabric8"));
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        UIContext uiContext = builder.getUIContext();
        String organisationsCacheKey = CacheNames.GITHUB_ORGANISATIONS;

        // TODO use different caches for organisations based on the git provider
        this.organisationsCache = cacheManager.getCache(organisationsCacheKey);

        this.github = gitHubFacadeFactory.createGitHubFacade(uiContext);

        if (github != null && github.isDetailsValid()) {
            String orgKey = github.getDetails().getUserCacheKey();
            organisations = organisationsCache.computeIfAbsent(orgKey, k -> github.loadGitHubOrganisations());
        }
        gitOrganisation.setValueChoices(organisations);
        gitOrganisation.setItemLabelConverter(GitOrganisationDTO::getName);
        String userName = github.getDetails().getUsername();
        if (Strings.isNotBlank(userName)) {
            for (GitOrganisationDTO organisation : organisations) {
                if (userName.equals(organisation.getName())) {
                    gitOrganisation.setDefaultValue(organisation);
                    break;
                }
            }
        }

        super.initializeUI(builder);
    }

    @Override
    public void validate(UIValidationContext context) {
        // lets ignore the mission validation as its not suitable for fabric8
        // super.validate(context);

        if (github == null || !github.isDetailsValid()) {
            // invoked too early before the github account is setup - lets return silently
            return;
        }
        String orgName = getOrganisationName(gitOrganisation.getValue());
        String repoName = getGithubRepositoryNameValue();

        if (Strings.isNotBlank(orgName)) {
            if (Strings.isNotBlank(repoName)) {
                github.validateRepositoryName(getNamed(), context, orgName, repoName);
            }
        }
    }


    @Override
    public NavigationResult next(UINavigationContext context) throws Exception {
        UIContext uiContext = context.getUIContext();
        storeAttributes(uiContext);
        return null;
    }

    protected void storeAttributes(UIContext uiContext) {
        // lets default the artifactId to the project name to ensure that
        // each project generates a different docker image name
        String projectName = getNamed().getValue();
        getArtifactId().setValue(projectName);
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        UIContext uiContext = context.getUIContext();
        storeAttributes(uiContext);
        if (github == null) {
            return Results.fail("No github account setup");
        }

        String org = getOrganisationName(gitOrganisation.getValue());
        String repo = getGitHubRepositoryName().getValue();
        if (Strings.isNullOrBlank(repo)) {
            repo = getNamed().getValue();
        }

        String orgOrNoUser = org;
        if (Strings.isNotBlank(org)) {
            orgOrNoUser = "user";
        }
        String orgAndRepo = orgOrNoUser + "/" + repo;
        LOG.info("Creating github repository " + orgAndRepo);

        File basedir = getSelectionFolder(uiContext);
        if (basedir == null || !basedir.exists() || !basedir.isDirectory()) {
            return Results.fail("No project directory exists! " + basedir);
        }

        GitHubImportParameters importParameters = new GitHubImportParameters(org, repo, orgAndRepo, github);
        uiContext.getAttributeMap().put(GitHubImportParameters.class, importParameters);

        return super.execute(context);
    }

    @Override
    protected boolean isShowArtifactId() {
        return false;
    }

    @Override
    protected void addDeploymentProperties(UIBuilder builder, DeploymentType deploymentType) {
        if (organisations.size() > 1) {
            builder.add(gitOrganisation);
        }
        // there's no need for github repo and name really - its just confusing and users may make mistakes?
        //builder.add(getGitHubRepositoryName());
        builder.add(getNamed());

    }
}
