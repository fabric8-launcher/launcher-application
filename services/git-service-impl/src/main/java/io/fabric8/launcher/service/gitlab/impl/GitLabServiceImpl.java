package io.fabric8.launcher.service.gitlab.impl;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.git.GitHelper;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.ImmutableGitHook;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.api.ImmutableGitRepository;
import io.fabric8.launcher.service.git.api.ImmutableGitUser;
import io.fabric8.launcher.service.git.api.NoSuchRepositoryException;
import io.fabric8.launcher.service.git.impl.AbstractGitService;
import io.fabric8.launcher.service.gitlab.api.GitLabEnvVarSysPropNames;
import io.fabric8.launcher.service.gitlab.api.GitLabService;
import io.fabric8.launcher.service.gitlab.api.GitLabWebhookEvent;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import static io.fabric8.launcher.service.git.GitHelper.checkGitRepositoryFullNameArgument;
import static io.fabric8.launcher.service.git.GitHelper.encode;
import static io.fabric8.launcher.service.git.GitHelper.execute;
import static io.fabric8.launcher.service.git.GitHelper.isValidGitRepositoryFullName;
import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class GitLabServiceImpl extends AbstractGitService implements GitLabService {

    private static final MediaType APPLICATION_FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded");

    private static final String GITLAB_URL = EnvironmentSupport.INSTANCE
            .getEnvVarOrSysProp(GitLabEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITLAB_URL, "https://gitlab.com");

    private final TokenIdentity identity;

    GitLabServiceImpl(final TokenIdentity identity) {
        super(identity);
        this.identity = identity;
    }

    @Override
    protected TokenIdentity getIdentity() {
        return identity;
    }

    @Override
    public List<GitOrganization> getOrganizations() {
        Request request = request()
                .get()
                .url(GITLAB_URL + "/api/v4/groups")
                .build();
        return execute(request, (JsonNode tree) -> {
            List<GitOrganization> orgs = new ArrayList<>();
            for (JsonNode node : tree) {
                orgs.add(ImmutableGitOrganization.of(node.get("path").asText()));
            }
            return orgs;
        }).orElse(Collections.emptyList());
    }

    @Override
    public List<GitRepository> getRepositories(GitOrganization organization) {
        String url;
        if (organization != null) {
            url = GITLAB_URL + "/api/v4/groups/" + organization.getName() + "/projects";
        } else {
            url = GITLAB_URL + "/api/v4/users/" + getLoggedUser().getLogin() + "/projects";
        }
        Request request = request()
                .get()
                .url(url)
                .build();
        return execute(request, (JsonNode tree) -> {
            List<GitRepository> orgs = new ArrayList<>();
            for (JsonNode node : tree) {
                orgs.add(readGitRepository(node));
            }
            return orgs;
        }).orElse(Collections.emptyList());
    }

    @Override
    public GitRepository createRepository(GitOrganization organization, String repositoryName, String description) throws IllegalArgumentException {
        StringBuilder content = new StringBuilder();
        content.append("name=").append(repositoryName);
        if (organization != null) {
            content.append("&namespace_id=").append(organization.getName());
        }
        if (description != null && !description.isEmpty()) {
            content.append("&description=").append(description);
        }
        Request request = request()
                .post(RequestBody.create(APPLICATION_FORM_URLENCODED, content.toString()))
                .url(GITLAB_URL + "/api/v4/projects")
                .build();
        return execute(request, GitLabServiceImpl::readGitRepository)
                .orElseThrow(() -> new NoSuchRepositoryException(repositoryName));
    }

    @Override
    public GitRepository createRepository(String repositoryName, String description) throws IllegalArgumentException {
        return createRepository(null, repositoryName, description);
    }

    @Override
    public Optional<GitRepository> getRepository(String name) {
        // Precondition checks
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("repository name must be specified");
        }
        if (isValidGitRepositoryFullName(name)) {
            String[] split = name.split("/");
            return getRepository(split[0], split[1]);
        } else {
            return getRepository(getLoggedUser().getLogin(), name);
        }
    }

    @Override
    public Optional<GitRepository> getRepository(GitOrganization organization, String repositoryName) {
        return getRepository(organization.getName(), repositoryName);
    }

    private Optional<GitRepository> getRepository(String owner, String repositoryName) {
        Request request = request()
                .get()
                .url(GITLAB_URL + "/api/v4/users/" + encode(owner) + "/projects?owned=true&search=" + encode(repositoryName))
                .build();
        return execute(request, tree ->
        {
            Iterator<JsonNode> iterator = tree.iterator();
            if (!iterator.hasNext()) {
                return null;
            }
            return readGitRepository(iterator.next());
        });
    }

    @Override
    public void deleteRepository(String repositoryFullName) throws IllegalArgumentException {
        checkGitRepositoryFullNameArgument(repositoryFullName);
        Request request = request()
                .delete()
                .url(GITLAB_URL + "/api/v4/projects/" +repositoryFullName)
                .build();
        execute(request, null);
    }

    @Override
    public GitHook createHook(GitRepository repository, String secret, URL webhookUrl, String... events) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(webhookUrl, "webhookUrl must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());

        if (events == null || events.length == 0) {
            events = getSuggestedNewHookEvents();
        }
        StringBuilder content = new StringBuilder();
        content.append("url=").append(webhookUrl);
        if (secret != null && secret.length() > 0) {
            content.append("&token=" + encode(secret));
        }
        for (String event : events) {
            content.append("&" + event.toLowerCase() + "_events=true");
        }
        Request request = request()
                .post(RequestBody.create(APPLICATION_FORM_URLENCODED, content.toString()))
                .url(GITLAB_URL + "/api/v4/projects/" + repository.getFullName() + "/hooks")
                .build();

        return execute(request, this::readHook).orElse(null);
    }

    @Override
    public List<GitHook> getHooks(GitRepository repository) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());
        Request request = request()
                .get()
                .url(GITLAB_URL + "/api/v4/projects/" + repository.getFullName() + "/hooks")
                .build();
        return execute(request, (JsonNode tree) ->
                StreamSupport.stream(tree.spliterator(), false)
                        .map(this::readHook)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public Optional<GitHook> getHook(GitRepository repository, URL url) throws IllegalArgumentException {
        if (url == null) {
            throw new IllegalArgumentException("URL should not be null");
        }
        return getHooks(repository).stream()
                .filter(h -> h.getUrl().equalsIgnoreCase(url.toString()))
                .findFirst();
    }

    @Override
    public void deleteWebhook(GitRepository repository, GitHook webhook) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(webhook, "webhook must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());

        Request request = request()
                .delete()
                .url(GITLAB_URL + "/api/v4/projects/" + repository.getFullName() + "/hooks/" + webhook.getName())
                .build();
        execute(request, null);
    }

    @Override
    public GitUser getLoggedUser() {
        Request request = request()
                .get()
                .url(GITLAB_URL + "/api/v4/user")
                .build();
        return execute(request, tree ->
                ImmutableGitUser.of(tree.get("username").asText(),
                                    tree.get("avatar_url").asText()))
                .orElseThrow(IllegalStateException::new);
    }

    private Request.Builder request() {
        return GitHelper.request(getIdentity());
    }

    private GitHook readHook(JsonNode tree) {
        ImmutableGitHook.Builder builder = ImmutableGitHook.builder()
                .name(tree.get("id").asText())
                .url(tree.get("url").asText());
        Iterator<Map.Entry<String, JsonNode>> fields = tree.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            if (fieldName.endsWith("_events") && entry.getValue().asBoolean()) {
                builder.addEvents(fieldName.substring(0, fieldName.lastIndexOf("_events")));
            }
        }
        return builder.build();
    }

    private static GitRepository readGitRepository(JsonNode node) {
        return ImmutableGitRepository.builder()
                .fullName(node.get("path_with_namespace").asText())
                .homepage(URI.create(node.get("web_url").asText()))
                .gitCloneUri(URI.create(node.get("http_url_to_repo").asText()))
                .build();
    }

    @Override
    public String[] getSuggestedNewHookEvents() {
        String[] events = {
                GitLabWebhookEvent.PUSH.name(),
                GitLabWebhookEvent.MERGE_REQUESTS.name(),
                GitLabWebhookEvent.ISSUES.name()
        };
        return events;
    }
}