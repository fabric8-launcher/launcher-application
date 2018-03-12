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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import io.fabric8.forge.generator.git.EnvironmentVariablePrefixes;
import io.fabric8.forge.generator.git.GitAccount;
import io.fabric8.forge.generator.git.GitOrganisationDTO;
import io.fabric8.forge.generator.git.GitRepositoryDTO;
import io.fabric8.forge.generator.git.WebHookDetails;
import io.fabric8.project.support.UserDetails;
import io.fabric8.utils.Strings;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.kohsuke.github.GHCreateRepositoryBuilder;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.unmodifiableMap;

/**
 */
public class GitHubFacade {
    private static final transient Logger LOG = LoggerFactory.getLogger(GitHubFacade.class);

    public static final String MY_PERSONAL_GITHUB_ACCOUNT = "My personal github account";

    private final GitAccount details;

    private GHMyself myself;

    private GitHub github;

    public GitHubFacade() {
        this(GitAccount.createViaEnvironmentVariables(EnvironmentVariablePrefixes.GITHUB));
    }

    public GitHubFacade(GitAccount details) {
        this.details = details;

        String username = details.getUsername();
        String token = details.getToken();
        String password = details.getPassword();

        try {
            final GitHubBuilder ghb = new GitHubBuilder();
            if (Strings.isNotBlank(username) && Strings.isNotBlank(password)) {
                ghb.withPassword(username, password);
            } else if (Strings.isNotBlank(token)) {
                if (Strings.isNotBlank(username)) {
                    ghb.withOAuthToken(token, username);
                } else {
                    ghb.withOAuthToken(token);
                }
            }
            this.github = ghb.build();
            this.myself = this.github.getMyself();
            String login = myself.getLogin();
            if (Strings.isNotBlank(login) && !Objects.equals(login, username)) {
                LOG.debug("Switching the github user name from " + username + " to " + login);
                details.setUsername(login);
            }
            // lets always use the github email address
            String email = myself.getEmail();
            if (Strings.isNotBlank(email)) {
                details.setEmail(email);
            }
        } catch (IOException e) {
            LOG.warn("Failed to create github client for user " + details.getUsername());
        }
    }


    public Collection<GitOrganisationDTO> loadGitHubOrganisations() {
        SortedSet<GitOrganisationDTO> organisations = new TreeSet<>();
        String username = details.getUsername();
        if (Strings.isNotBlank(username)) {
            organisations.add(new GitOrganisationDTO(username, MY_PERSONAL_GITHUB_ACCOUNT));
        }
        GitHub github = this.github;
        if (github != null) {
            try {
                LOG.debug("Loading github organisations for " + username);
                Map<String, GHOrganization> map = github.getMyOrganizations();
                if (map != null) {
                    Collection<GHOrganization> organizations = map.values();
                    for (GHOrganization organization : organizations) {
                        GitOrganisationDTO dto = new GitOrganisationDTO(organization, username);
                        if (dto.isValid()) {
                            organisations.add(dto);
                        }
                    }
                }
            } catch (HttpException e) {
                if (e.getResponseCode() == 403) {
                    // don't have the karma for listing organisations
                    LOG.warn("User doesn't have karma to list organisations: " + e);
                    return organisations;
                } else {
                    LOG.warn("Failed to load github organisations for user: " + details.getUsername() + " due to : " + e, e);
                }
            } catch (IOException e) {
                LOG.warn("Failed to load github organisations for user: " + details.getUsername() + " due to : " + e, e);
            }
        }
        return organisations;
    }

    public void validateRepositoryName(UIInput<String> input, UIValidationContext context, String orgName,
                                       String repoName) {
        GitHub github = this.github;
        if (github != null) {
            String name = orgName + "/" + repoName;
            try {
                GHRepository repository = github.getRepository(name);
                if (repository != null) {
                    context.addValidationError(input, "The repository " + repoName + " already exists!");
                }
            } catch (FileNotFoundException e) {
                // repo doesn't exist
            } catch (IOException e) {
                LOG.warn("Caught exception looking up github repository " + name + ". " + e, e);
            }
        }
    }

    public Collection<GitRepositoryDTO> getRepositoriesForOrganisation(String orgName) {
        Set<GitRepositoryDTO> answer = new TreeSet<>();
        GitHub github = this.github;
        if (github != null) {
            try {

                Map<String, GHRepository> repositories;
                String username = details.getUsername();
                if (Strings.isNullOrBlank(orgName) || orgName.equals(username)) {
                    Map<String, GHRepository> repositoriesTree = new TreeMap<>();
                    // With OWNER, retrieve public and private repositories owned by current user (only).
                    for (GHRepository r : github.getMyself().listRepositories(100, GHMyself.RepositoryListFilter.OWNER)) {
                        repositoriesTree.put(r.getName(), r);
                    }
                    repositories = unmodifiableMap(repositoriesTree);
                } else {
                    repositories = github.getOrganization(orgName).getRepositories();
                }
                if (repositories != null) {
                    for (Map.Entry<String, GHRepository> entry : repositories.entrySet()) {
                        String key = entry.getKey();
                        GHRepository repository = entry.getValue();
                        answer.add(new GitRepositoryDTO(key, repository));
                    }
                }
            } catch (IOException e) {
                LOG.warn("Caught exception looking up github repositories for " + orgName + ". " + e, e);
            }
        }
        return answer;
    }

    public UserDetails createUserDetails(String gitUrl) {
        return new UserDetails(gitUrl, gitUrl, details.getUsername(), details.tokenOrPassword(), getEmail());
    }

    public boolean hasFile(String org, String repoName, String fileName) {
        boolean hasFile = false;
        try {
            if (github.getRepository(org + "/" + repoName).getFileContent(fileName) != null) {
                hasFile = true;
            }
        } catch (IOException e) {
            return hasFile;
        }
        return hasFile;
    }

    public GHMyself getMyself() {
        if (myself == null) {
            try {
                myself = this.github.getMyself();
                if (myself == null) {
                    LOG.warn("Could not find valid github.getMyself()");
                }
            } catch (IOException e) {
                LOG.warn("Could not load github.getMyself(): " + e, e);
            }

        }
        return myself;
    }

    public String getEmail() {
        String email = details.getEmail();
        if (Strings.isNullOrBlank(email)) {
            GHMyself gitMyself = getMyself();
            if (gitMyself != null) {
                try {
                    email = gitMyself.getEmail();
                    if (Strings.isNullOrBlank(email)) {
                        // github user might have chosen to keep email address private
                        // populate it with invalid address as null is invalid for org.eclipse.jgit.lib.PersonIdent
                        email = "openshiftio@redhat.com";

                    }
                } catch (IOException e) {
                    LOG.warn("Could not get github.getMyself().getEmail(): " + e, e);
                }
            }
        }
        return email;
    }

    public GitAccount getDetails() {
        return details;
    }

    public GHRepository createRepository(String orgName, String repoName, String description) throws IOException {
        GHCreateRepositoryBuilder builder;
        if (Strings.isNullOrBlank(orgName) || orgName.equals(details.getUsername())) {
            builder = github.createRepository(repoName);
        } else {
            builder = github.getOrganization(orgName).createRepository(repoName);
        }
        // TODO link to the space URL?
        builder.private_(false)
                .homepage("")
                .issues(false)
                .downloads(false)
                .wiki(false);

        if (Strings.isNotBlank(description)) {
            builder.description(description);
        }
        return builder.create();
    }

    public boolean isDetailsValid() {
        return details != null && GitAccount.isValid(details);
    }

    public void createWebHook(WebHookDetails webhook) throws IOException {
        String repoName = webhook.getRepositoryName();


        String orgName = webhook.getGitOwnerName();
        GHRepository repository = github.getRepository(orgName + "/" + repoName);
        String webhookUrl = webhook.getWebhookUrl();

        removeOldWebHooks(repository, webhookUrl);

        Map<String, String> config = new HashMap<>();
        config.put("url", webhookUrl);
        config.put("insecure_ssl", "1");
        config.put("content_type", "json");
        config.put("secret", webhook.getSecret());
        List<GHEvent> events = new ArrayList<>();
        events.add(GHEvent.PUSH);
        events.add(GHEvent.PULL_REQUEST);
        events.add(GHEvent.ISSUE_COMMENT);
        GHHook hook = repository.createHook("web", config, events, true);
        if (hook != null) {
            LOG.info("Created WebHook " + hook.getName() + " with ID " + hook.getId() + " for " + repository.getFullName() + " on URL " + webhookUrl);
        }
        //registerGitWebHook(details, webhook.getWebhookUrl(), webhook.getGitOwnerName(), repoName, webhook.getSecret());
    }

    private void removeOldWebHooks(GHRepository repository, String webhookUrl) {
        List<GHHook> hooks;
        try {
            hooks = repository.getHooks();
        } catch (IOException e) {
            LOG.warn("Failed to find WebHooks for repository " + repository.getFullName() + " due to : " + e, e);
            return;
        }
        if (hooks != null) {
            for (GHHook hook : hooks) {
                Map<String, String> config = hook.getConfig();
                if (config != null) {
                    String url = config.get("url");
                    if (url != null && webhookUrl.equals(url)) {
                        LOG.info("Removing WebHook " + hook.getName() + " with ID " + hook.getId() + " for " + repository.getFullName() + " on URL " + webhookUrl);
                        try {
                            hook.delete();
                        } catch (IOException e) {
                            LOG.warn("Failed to remove WebHook " + hook.getName() + " with ID " + hook.getId() + " for " + repository.getFullName() + " on URL " + webhookUrl + " due to: " + e, e);
                        }
                    }
                }
            }
        }
    }
}
