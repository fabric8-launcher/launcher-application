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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.fabric8.forge.generator.github.GitHubProvider;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.project.support.GitUtils;
import io.fabric8.project.support.UserDetails;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class GitProvider {
    private static final transient Logger LOG = LoggerFactory.getLogger(GitProvider.class);

    private final String name;

    public GitProvider(String name) {
        this.name = name;
    }

    public static List<GitProvider> loadGitProviders() {
        List<GitProvider> answer = new ArrayList<>();
        answer.add(new GitHubProvider());

        LOG.debug("Loaded git providers: " + answer);
        return answer;
    }

    protected static boolean hasService(KubernetesClient kubernetesClient, String namespace, String name) {
        try {
            Service service = kubernetesClient.services().inNamespace(namespace).withName(name).get();
            return service != null;
        } catch (Exception e) {
            LOG.warn("Failed to find service " + namespace + "/" + name + ". " + e, e);
            return false;
        }
    }

    public static GitProvider pickDefaultGitProvider(List<GitProvider> gitProviders) {
        if (gitProviders.isEmpty()) {
            return null;
        }
        return gitProviders.get(0);
    }

    @Override
    public String toString() {
        return "GitProvider{" +
                "name='" + name + '\'' +
                '}';
    }

    public boolean isGitHub() {
        return "github".equals(getName());
    }

    public String getName() {
        return name;
    }

    public abstract void addCreateRepositoryStep(NavigationResultBuilder builder);

    public abstract void addImportRepositoriesSteps(NavigationResultBuilder builder);

    public abstract boolean isConfiguredCorrectly();

    public abstract void addConfigureStep(NavigationResultBuilder builder);

    public abstract void registerWebHook(GitAccount details, WebHookDetails webhook) throws IOException;

    public Git cloneRepo(CloneRepoAttributes attributes) throws GitAPIException {
        CloneCommand command = Git.cloneRepository();
        String gitUri = attributes.getUri();

        UserDetails userDetails = attributes.getUserDetails();
        CredentialsProvider credentialsProvider = userDetails.createCredentialsProvider();
        GitUtils.configureCommand(command, credentialsProvider, userDetails.getSshPrivateKey(), userDetails.getSshPublicKey());

        command = command.setCredentialsProvider(credentialsProvider).
                setCloneAllBranches(attributes.isCloneAll()).
                setURI(gitUri).
                setDirectory(attributes.getDirectory()).setRemote(attributes.getRemote());

        return command.call();
    }

    public abstract void addGitCloneStep(NavigationResultBuilder builder);
}
