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
package io.fabric8.forge.generator.pipeline;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.fabric8.forge.addon.utils.StopWatch;
import io.fabric8.project.support.GitUtils;
import io.fabric8.project.support.UserDetails;
import io.fabric8.utils.Files;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.jboss.forge.furnace.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Singleton
public class JenkinsPipelineLibrary {
    private static final transient Logger LOG = LoggerFactory.getLogger(JenkinsPipelineLibrary.class);

    private final File workflowFolder;

    private final String remote;

    private final String jenkinsfileLibraryGitUrl;

    private final String jenkinsfileLibraryGitTag;

    @Inject
    public JenkinsPipelineLibrary() {
        this.jenkinsfileLibraryGitUrl = getSystemPropertyOrDefault("JENKINSFILE_LIBRARY_GIT_REPOSITORY",
                                                                   "https://github.com/fabric8io/fabric8-jenkinsfile-library.git");
        this.jenkinsfileLibraryGitTag = getSystemPropertyOrDefault("JENKINSFILE_LIBRARY_GIT_TAG", null);
        this.remote = getSystemPropertyOrDefault("GIT_REMOTE_BRANCH_NAME", "origin");
        LOG.info("Using jenkins workflow library: " + this.jenkinsfileLibraryGitUrl);
        LOG.info("Using jenkins workflow library version: " + this.jenkinsfileLibraryGitTag);

        String workflowDir = getSystemPropertyOrDefault("JENKINSFILE_LIBRARY_DIR", "target/jenkinsfileLibrary");
        workflowFolder = new File(workflowDir);
        LOG.info("Using Jenkinsfile library at: " + workflowFolder);
        cloneOrPull();
    }

    public static String getSystemPropertyOrDefault(String envVarName, String defaultValue) {
        String answer = System.getenv(envVarName);
        if (Strings.isNullOrEmpty(answer)) {
            return defaultValue;
        }
        return answer;
    }

    public static void cloneRepo(File projectFolder, String cloneUrl, CredentialsProvider credentialsProvider,
                                 final File sshPrivateKey, final File sshPublicKey, String remote) {
        cloneRepo(projectFolder, cloneUrl, credentialsProvider, sshPrivateKey, sshPublicKey, remote, null);
    }

    public static void cloneRepo(File projectFolder, String cloneUrl, CredentialsProvider credentialsProvider,
                                 final File sshPrivateKey, final File sshPublicKey, String remote, String tag) {
        StopWatch watch = new StopWatch();

        // clone the repo!
        boolean cloneAll = true;
        LOG.info("Cloning git repo " + cloneUrl + " into directory " + projectFolder.getAbsolutePath()
                         + " cloneAllBranches: " + cloneAll);
        CloneCommand command = Git.cloneRepository();
        GitUtils.configureCommand(command, credentialsProvider, sshPrivateKey, sshPublicKey);
        command = command.setCredentialsProvider(credentialsProvider).
                setCloneAllBranches(cloneAll).setURI(cloneUrl).setDirectory(projectFolder).setRemote(remote);

        try {
            Git git = command.call();
            if (tag != null) {
                git.checkout().setName(tag).call();
            }
        } catch (Throwable e) {
            LOG.error("Failed to command remote repo " + cloneUrl + " due: " + e.getMessage(), e);
            throw new RuntimeException("Failed to command remote repo " + cloneUrl + " due: " + e.getMessage());
        } finally {
            LOG.debug("cloneRepo took " + watch.taken());
        }
    }

    private void cloneOrPull() {
        StopWatch watch = new StopWatch();
        try {
            LOG.debug("Cloning or pulling jenkins workflow repo from " + jenkinsfileLibraryGitUrl + " to "
                              + workflowFolder);
            UserDetails anonymous = createAnonymousDetails();
            cloneOrPullRepo(anonymous, workflowFolder, jenkinsfileLibraryGitUrl, null, null);
        } catch (Exception e) {
            LOG.error("Failed to clone jenkins workflow repo from : " + jenkinsfileLibraryGitUrl + ". " + e, e);
        } finally {
            LOG.debug("asyncCloneOrPullJenkinsWorkflows took " + watch.taken());
        }
    }

    private UserDetails createAnonymousDetails() {
        return new UserDetails("", "", "", "", "");
    }

    public File getWorkflowFolder() {
        return workflowFolder;
    }

    public File cloneOrPullRepo(UserDetails userDetails, File projectFolder, String cloneUrl, File sshPrivateKey,
                                File sshPublicKey) {
        File gitFolder = new File(projectFolder, ".git");
        CredentialsProvider credentialsProvider = userDetails.createCredentialsProvider();
        if (!Files.isDirectory(gitFolder) || !Files.isDirectory(projectFolder)) {
            // lets clone the git repository!
            cloneRepo(projectFolder, cloneUrl, credentialsProvider, sshPrivateKey, sshPublicKey, this.remote,
                      this.jenkinsfileLibraryGitTag);
        } else {
            doPull(gitFolder, credentialsProvider, userDetails.getBranch(), userDetails.createPersonIdent(), userDetails);
        }
        return projectFolder;
    }

    public File cloneRepoIfNotExist(UserDetails userDetails, File projectFolder, String cloneUrl) {
        File gitFolder = new File(projectFolder, ".git");
        CredentialsProvider credentialsProvider = userDetails.createCredentialsProvider();
        if (!Files.isDirectory(gitFolder) || !Files.isDirectory(projectFolder)) {
            // lets clone the git repository!
            cloneRepo(projectFolder, cloneUrl, credentialsProvider, userDetails.getSshPrivateKey(),
                      userDetails.getSshPublicKey(), this.remote);

        }
        return projectFolder;
    }

    protected void doPull(File gitFolder, CredentialsProvider cp, String branch, PersonIdent personIdent,
                          UserDetails userDetails) {
        StopWatch watch = new StopWatch();
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(gitFolder)
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();

            Git git = new Git(repository);

            File projectFolder = repository.getDirectory();

            StoredConfig config = repository.getConfig();
            String url = config.getString("remote", remote, "url");
            if (io.fabric8.utils.Strings.isNullOrBlank(url)) {
                LOG.warn("No remote repository url for " + branch + " defined for the git repository at " + projectFolder
                        .getCanonicalPath() + " so cannot pull");
                //return;
            }
            String mergeUrl = config.getString("branch", branch, "merge");
            if (io.fabric8.utils.Strings.isNullOrBlank(mergeUrl)) {
                LOG.warn("No merge spec for branch." + branch + ".merge in the git repository at " + projectFolder
                        .getCanonicalPath() + " so not doing a pull");
                //return;
            }

            // lets trash any failed changes
            LOG.debug("Stashing local changes to the repo");
            boolean hasHead = true;
            try {
                git.log().all().call();
                hasHead = git.getRepository().getAllRefs().containsKey("HEAD");
            } catch (NoHeadException e) {
                hasHead = false;
            }
            if (hasHead) {
                // lets stash any local changes just in case..
                try {
                    git.stashCreate().setPerson(personIdent).setWorkingDirectoryMessage("Stash before a write")
                            .setRef("HEAD").call();
                } catch (Throwable e) {
                    LOG.error("Failed to stash changes: " + e, e);
                    Throwable cause = e.getCause();
                    if (cause != null && cause != e) {
                        LOG.error("Cause: " + cause, cause);
                    }
                }
            }

            //LOG.debug("Resetting the repo");
            //git.reset().setMode(ResetCommand.ResetType.HARD).call();

            LOG.debug("Performing a pull in git repository " + projectFolder.getCanonicalPath() + " on remote URL: " + url);
            PullCommand pull = git.pull();
            GitUtils.configureCommand(pull, userDetails);
            pull.setRebase(true).call();
        } catch (Throwable e) {
            LOG.error("Failed to pull from the remote git repo with credentials " + cp + " due: " + e.getMessage()
                              + ". This exception is ignored.", e);
        } finally {
            LOG.debug("doPull took " + watch.taken());
        }
    }
}
