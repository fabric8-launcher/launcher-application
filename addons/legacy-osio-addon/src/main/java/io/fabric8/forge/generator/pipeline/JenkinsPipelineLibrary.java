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
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.fabric8.forge.addon.utils.StopWatch;
import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.Paths;
import io.fabric8.project.support.GitUtils;
import io.fabric8.project.support.UserDetails;
import io.fabric8.utils.Files;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
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
        this.jenkinsfileLibraryGitUrl = "https://github.com/fabric8io/fabric8-jenkinsfile-library.git";
        this.jenkinsfileLibraryGitTag = "v2.2.336";
        this.remote = "origin";
        LOG.info("Using jenkins workflow library: " + this.jenkinsfileLibraryGitUrl);
        LOG.info("Using jenkins workflow library version: " + this.jenkinsfileLibraryGitTag);

        String workflowDir = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("JENKINSFILE_LIBRARY_DIR", "target/jenkinsfileLibrary");
        workflowFolder = new File(workflowDir);
        LOG.info("Using Jenkinsfile library at: " + workflowFolder);
        deleteIfExistsAndClone();
    }

    File getWorkflowFolder() {
        return workflowFolder;
    }

    private void deleteIfExistsAndClone() {
        StopWatch watch = new StopWatch();
        try {
            LOG.debug("Cloning or pulling jenkins workflow repo from " + jenkinsfileLibraryGitUrl + " to "
                              + workflowFolder);
            UserDetails anonymous = createAnonymousDetails();
            deleteIfExistsAndClone(anonymous, workflowFolder, jenkinsfileLibraryGitUrl, null, null);
        } catch (Exception e) {
            LOG.error("Failed to clone jenkins workflow repo from : " + jenkinsfileLibraryGitUrl + ". " + e, e);
        } finally {
            LOG.debug("asyncCloneOrPullJenkinsWorkflows took " + watch.taken());
        }
    }

    private File deleteIfExistsAndClone(UserDetails userDetails, File projectFolder, String cloneUrl, File sshPrivateKey,
                                        File sshPublicKey) {
        CredentialsProvider credentialsProvider = userDetails.createCredentialsProvider();
        if (Files.isDirectory(projectFolder)) {
            LOG.info("Deleting existing project folder " + projectFolder.getAbsolutePath());
            try {
                Paths.deleteDirectory(projectFolder.toPath());
            } catch (IOException e) {
                LOG.error("Error while deleting existing project folder " + projectFolder.getAbsolutePath(), e);
            }
        }
        // lets clone the git repository!
        cloneRepo(projectFolder, cloneUrl, credentialsProvider, sshPrivateKey, sshPublicKey, this.remote,
                  this.jenkinsfileLibraryGitTag);
        return projectFolder;
    }

    private static void cloneRepo(File projectFolder, String cloneUrl, CredentialsProvider credentialsProvider,
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

    private UserDetails createAnonymousDetails() {
        return new UserDetails("", "", "", "", "");
    }

}
