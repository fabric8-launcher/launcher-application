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
import java.io.IOException;
import java.util.Collection;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.devops.ProjectConfigs;
import io.fabric8.forge.generator.cache.CacheFacade;
import io.fabric8.forge.generator.pipeline.AbstractDevToolsCommand;
import io.fabric8.project.support.GitUtils;
import io.fabric8.project.support.UserDetails;
import io.fabric8.utils.IOHelpers;
import io.fabric8.utils.Strings;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.PersonIdent;
import org.infinispan.Cache;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class AbstractGitRepoStep extends AbstractDevToolsCommand {
    final transient Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final String accountsCacheKey;
    private final String organisationsCacheKey;

    protected Cache<String, GitAccount> accountCache;
    protected Cache<String, Collection<GitOrganisationDTO>> organisationsCache;
    @Inject
    protected CacheFacade cacheManager;
    /**
     * The name of the upstream repo
     */
    private String origin = "origin";
    /**
     * The default branch we make on creating repos
     */
    private String branch = "master";

    @Inject
    public AbstractGitRepoStep(String accountsCacheKey, String organisationsCacheKey) {
        this.accountsCacheKey = accountsCacheKey;
        this.organisationsCacheKey = organisationsCacheKey;
    }

    public AbstractGitRepoStep(String accountsCacheKey, String organisationsCacheKey, CacheFacade cacheManager) {
        this.accountsCacheKey = accountsCacheKey;
        this.organisationsCacheKey = organisationsCacheKey;
        this.cacheManager = cacheManager;
    }



    public static String getOrganisationName(GitOrganisationDTO org) {
        String orgName = null;
        if (org != null) {
            orgName = org.getId();
        }
        return orgName;
    }

    public void initializeUI(final UIBuilder builder) throws Exception {
        if (accountsCacheKey != null) {
            this.accountCache = cacheManager.getCache(accountsCacheKey);
        }
        if (organisationsCacheKey != null) {
            this.organisationsCache = cacheManager.getCache(organisationsCacheKey);
        }
    }

    public void importNewGitProject(UserDetails userDetails, File basedir, String message, String gitUrl)
            throws GitAPIException, JsonProcessingException {
        importNewGitProject(userDetails, basedir, message, gitUrl, this.branch, this.origin, LOG);
    }

    public static void importNewGitProject(UserDetails userDetails, File basedir, String message, String gitUrl, String branch, String origin, Logger logger) throws GitAPIException {
        GitUtils.disableSslCertificateChecks();
        InitCommand initCommand = Git.init();
        initCommand.setDirectory(basedir);
        Git git = initCommand.call();
        logger.info("Initialised an empty git configuration repo at {}", basedir.getAbsolutePath());
        gitAddCommitAndPush(git, gitUrl, userDetails, basedir, message, branch, origin, logger);
    }

    protected void gitAddCommitAndPush(Git git, String gitUrl, UserDetails userDetails, File basedir, String message) throws GitAPIException {
        gitAddCommitAndPush(git, gitUrl, userDetails, basedir, message, this.branch, this.origin, LOG);
    }

    private static void gitAddCommitAndPush(Git git, String gitUrl, UserDetails userDetails, File basedir, String message, String branch, String origin, Logger logger) throws GitAPIException {
        PersonIdent personIdent = userDetails.createPersonIdent();

        GitUtils.configureBranch(git, branch, origin, gitUrl);
        GitUtils.addDummyFileToEmptyFolders(basedir);
        logger.info("About to git commit and push to: " + gitUrl + " and remote name " + origin);
        int retryCount = 5;
        for (int i = retryCount; i > 0; i--) {
            if (i < retryCount) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            try {
                GitUtils.doAddCommitAndPushFiles(git, userDetails, personIdent, branch, origin, message, true);
                return;
            } catch (TransportException e) {
                if (i <= 1) {
                    throw e;
                } else {
                    logger.info("Caught a transport exception: " + e + " so retrying");
                }
            }
        }
    }

    protected Result updateGitURLInJenkinsfile(File basedir, String gitUrl) {
        return updateGitURLInJenkinsfile(basedir, gitUrl, LOG);
    }

    public static Result updateGitURLInJenkinsfile(File basedir, String gitUrl, Logger logger) {
        File jenkinsFile = new File(basedir, ProjectConfigs.LOCAL_FLOW_FILE_NAME);
        if (jenkinsFile.isFile() && jenkinsFile.exists()) {
            String pipelineText;
            try {
                pipelineText = IOHelpers.readFully(jenkinsFile);
            } catch (IOException e) {
                return Results.fail("Failed to load file " + jenkinsFile + ". " + e, e);
            }
            pipelineText = Strings.replaceAllWithoutRegex(pipelineText, "GIT_URL", "'" + gitUrl + "'");
            try {
                IOHelpers.writeFully(jenkinsFile, pipelineText);
            } catch (IOException e) {
                logger.error("Failed to write file " + jenkinsFile + ". " + e, e);
                return Results.fail("Failed to write file " + jenkinsFile + ". " + e, e);
            }
        }
        return null;
    }
}
