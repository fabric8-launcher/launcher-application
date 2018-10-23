package io.fabric8.launcher.service.git.gitea;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.http.AuthorizationType;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.git.AbstractGitService;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitRepositoryFilter;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.ImmutableGitHook;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.api.ImmutableGitRepository;
import io.fabric8.launcher.service.git.api.NoSuchOrganizationException;
import io.fabric8.launcher.service.git.gitea.api.GiteaEnvironment;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import static io.fabric8.launcher.base.JsonUtils.toList;
import static io.fabric8.launcher.base.http.Requests.APPLICATION_JSON;
import static io.fabric8.launcher.base.http.Requests.securedRequest;
import static io.fabric8.launcher.service.git.Gits.checkGitRepositoryFullNameArgument;
import static io.fabric8.launcher.service.git.Gits.checkGitRepositoryNameArgument;
import static io.fabric8.launcher.service.git.Gits.isValidGitRepositoryFullName;
import static io.fabric8.launcher.service.git.gitea.api.GiteaWebhookEvent.ISSUE_COMMENT;
import static io.fabric8.launcher.service.git.gitea.api.GiteaWebhookEvent.PULL_REQUEST;
import static io.fabric8.launcher.service.git.gitea.api.GiteaWebhookEvent.PUSH;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class GiteaService extends AbstractGitService implements GitService {

    private static final Logger log = Logger.getLogger(GiteaService.class.getName());

    private static final String GITEA_URL = GiteaEnvironment.LAUNCHER_BACKEND_GITEA_URL.value("https://try.gitea.io");

    private static final String GITEA_USERNAME = GiteaEnvironment.LAUNCHER_BACKEND_GITEA_USERNAME.value("admin");

    private static final String SUDO_HEADER = "Sudo";

    private final HttpClient httpClient;

    private final String impersonateUser;

    GiteaService(Identity identity, String impersonateUser, HttpClient httpClient) {
        super(identity);
        this.impersonateUser = impersonateUser;
        this.httpClient = httpClient;
    }

    @Override
    public List<GitOrganization> getOrganizations() {
        Request request = sudoRequest("/api/v1/user/orgs").build();
        Optional<List<GitOrganization>> orgs =
                httpClient.executeAndParseJson(request, node -> toList(node, GiteaService::toGitOrganization));
        return orgs.orElse(emptyList());
    }

    @Override
    public GitRepository createRepository(GitOrganization organization, String repositoryName, String description) throws IllegalArgumentException {
        // Precondition checks
        checkGitRepositoryNameArgument(repositoryName);
        requireNonNull(description, "description must be specified.");
        if (description.isEmpty()) {
            throw new IllegalArgumentException("description must not be empty.");
        }

        if (!getOrganizations().contains(organization)) {
            throw new NoSuchOrganizationException("User does not belong to organization '" + organization.getName() + "' or the organization does not exist");
        }
        ObjectNode payload =
                new JsonNodeFactory(false).objectNode()
                        .put("name", repositoryName)
                        .put("description", description);
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(APPLICATION_JSON, JsonUtils.toString(payload));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error while creating sudoRequest body", e);
        }
        String path = format("/api/v1/org/%s/repos", organization.getName());
        Request request = sudoRequest(path).post(requestBody).build();
        Optional<GitRepository> repository = httpClient.executeAndParseJson(request, GiteaService::toGitRepository);
        GitRepository gitRepository = repository.orElseThrow(() -> new IllegalStateException("Could not create repository " + repositoryName));
        addAdminUserAsCollaborator(gitRepository);
        return gitRepository;
    }

    @Override
    public GitRepository createRepository(String repositoryName, String description) throws IllegalArgumentException {
        // Precondition checks
        checkGitRepositoryNameArgument(repositoryName);
        requireNonNull(description, "description must be specified.");
        if (description.isEmpty()) {
            throw new IllegalArgumentException("description must not be empty.");
        }
        if (isValidGitRepositoryFullName(repositoryName)) {
            String[] split = repositoryName.split("/");
            ImmutableGitOrganization organization = ImmutableGitOrganization.of(split[0]);
            return createRepository(organization, split[1], description);
        }

        ObjectNode payload =
                new JsonNodeFactory(false).objectNode()
                        .put("name", repositoryName)
                        .put("description", description);
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(APPLICATION_JSON, JsonUtils.toString(payload));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error while creating sudoRequest body", e);
        }
        Request request = sudoRequest("/api/v1/user/repos").post(requestBody).build();
        Optional<GitRepository> repository = httpClient.executeAndParseJson(request, GiteaService::toGitRepository);
        GitRepository gitRepository = repository.orElseThrow(() -> new IllegalStateException("Could not create repository " + repositoryName));
        addAdminUserAsCollaborator(gitRepository);
        return gitRepository;
    }

    @Override
    public List<GitRepository> getRepositories(GitRepositoryFilter filter) {
        requireNonNull(filter, "filter must be specified.");

        StringBuilder param = new StringBuilder("exclusive=1");
        param.append("&uid=").append(getLoggedUser().getId());
        if (StringUtils.isNotBlank(filter.withNameContaining())) {
            param.append("&q=").append(filter.withNameContaining());
        }
        if (filter.withOrganization() != null) {
            if (!getOrganizations().contains(filter.withOrganization())) {
                throw new NoSuchOrganizationException("User does not belong to organization '" + filter.withOrganization().getName() + "' or the organization does not exist");
            }
            param.append("&mode=collaborative");
        } else {
            param.append("&mode=source");
        }

        Request request = sudoRequest("/api/v1/repos/search?" + param)
                .get().build();
        Optional<List<GitRepository>> repositories =
                httpClient.executeAndParseJson(request, node -> toList(node.get("data"), GiteaService::toGitRepository));
        List<GitRepository> gitRepositories = repositories.orElse(emptyList());
        if (filter.withOrganization() != null) {
            return gitRepositories.stream()
                    .filter(r -> r.getFullName().startsWith(filter.withOrganization().getName()))
                    .collect(Collectors.toList());
        }
        return gitRepositories;
    }

    @Override
    public Optional<GitRepository> getRepository(String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("repositoryName must not be empty.");
        }
        if (isValidGitRepositoryFullName(name)) {
            return getRepositoryByFullName(name);
        } else {
            return getRepositoryByFullName(impersonateUser + "/" + name);
        }
    }

    @Override
    public Optional<GitRepository> getRepository(GitOrganization organization, String repositoryName) {
        requireNonNull(organization, "organization must not be null.");
        checkGitRepositoryNameArgument(repositoryName);
        if (repositoryName.isEmpty()) {
            throw new IllegalArgumentException("repositoryName must not be empty.");
        }
        if (!getOrganizations().contains(organization)) {
            throw new NoSuchOrganizationException("User does not belong to organization '" + organization.getName() + "' or the organization does not exist");
        }
        return getRepositoryByFullName(organization.getName() + "/" + repositoryName);
    }

    private Optional<GitRepository> getRepositoryByFullName(String repositoryFullName) {
        checkGitRepositoryFullNameArgument(repositoryFullName);

        String path = format("/api/v1/repos/%s", repositoryFullName);
        Request request = sudoRequest(path).get().build();
        return httpClient.executeAndParseJson(request, GiteaService::toGitRepository);
    }


    @Override
    public GiteaUser getLoggedUser() {
        Request request = sudoRequest("/api/v1/user").get().build();
        return httpClient.executeAndParseJson(request, GiteaService::toGitUser).
                orElseThrow(() -> new IllegalStateException("Cannot get current user info"));
    }

    @Override
    public Optional<GitHook> getHook(GitRepository repository, URL url) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(url, "url must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());

        return getHooks(repository).stream()
                .filter(h -> h.getUrl().equalsIgnoreCase(url.toString()))
                .findFirst();
    }

    @Override
    public List<GitHook> getHooks(GitRepository repository) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());

        Request request = sudoRequest(format("/api/v1/repos/%s/hooks", repository.getFullName())).get().build();
        return httpClient.executeAndParseJson(request, node -> toList(node, GiteaService::toGitHook))
                .orElseThrow(() -> new IllegalStateException("Cannot retrieve hooks for repository " + repository));
    }

    @Override
    public GitHook createHook(GitRepository repository, @Nullable String secret, URL webhookUrl, String... events) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(webhookUrl, "webhookUrl must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());

        JsonNodeFactory factory = new JsonNodeFactory(false);
        ArrayNode eventsNode = factory.arrayNode();
        if (events == null || events.length == 0) {
            events = getSuggestedNewHookEvents();
        }
        for (String event : events) {
            eventsNode.add(event);
        }
        ObjectNode payload = factory.objectNode()
                .put("active", true)
                .put("type", "gitea");
        payload.set("events", eventsNode);
        payload.set("config", factory.objectNode()
                .put("content_type", "json")
                .put("url", webhookUrl.toString())
                .put("secret", secret)
        );
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(APPLICATION_JSON, JsonUtils.toString(payload));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error while creating sudoRequest body", e);
        }
        Request request = sudoRequest(format("/api/v1/repos/%s/hooks", repository.getFullName())).post(requestBody).build();
        return httpClient.executeAndParseJson(request, GiteaService::toGitHook)
                .orElseThrow(() -> new IllegalStateException("Hook could not be created"));
    }

    @Override
    public void deleteRepository(String repositoryFullName) throws IllegalArgumentException {
        checkGitRepositoryFullNameArgument(repositoryFullName);

        Request request = sudoRequest(format("/api/v1/repos/%s", repositoryFullName)).delete().build();
        httpClient.execute(request);
    }

    @Override
    public void deleteWebhook(GitRepository repository, GitHook webhook) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(webhook, "webhook must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());
        Request request = sudoRequest(format("/api/v1/repos/%s/hooks/%s", repository.getFullName(), webhook.getName())).delete().build();
        httpClient.execute(request);
    }

    @Override
    protected void setCredentialsProvider(Consumer<CredentialsProvider> consumer) {
        getIdentity().accept(new IdentityVisitor() {
            @Override
            public void visit(TokenIdentity token) {
                consumer.accept(new UsernamePasswordCredentialsProvider(GITEA_USERNAME, token.getToken()));
            }
        });
    }

    @Override
    public String[] getSuggestedNewHookEvents() {
        return new String[]{
                PUSH.id(),
                PULL_REQUEST.id(),
                ISSUE_COMMENT.id()
        };
    }

    /**
     * Adds the admin user as a collaborator in the created repository
     *
     * Needed until https://github.com/go-gitea/gitea/issues/4292 is fixed
     *
     * @param repository
     */
    private void addAdminUserAsCollaborator(GitRepository repository) {
        ObjectNode payload = new JsonNodeFactory(false)
                .objectNode()
                .put("permission", "write");

        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(APPLICATION_JSON, JsonUtils.toString(payload));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error while creating sudoRequest body", e);
        }

        Request request = sudoRequest(format("/api/v1/repos/%s/collaborators/%s", repository.getFullName(), GITEA_USERNAME))
                .put(requestBody).build();
        httpClient.execute(request);
    }

    private Request.Builder sudoRequest(String path) {
        return request(path).header(SUDO_HEADER, impersonateUser);
    }

    private Request.Builder request(String path) {
        return securedRequest(getIdentity(), AuthorizationType.TOKEN)
                .url(GITEA_URL + path);
    }


    private static GitOrganization toGitOrganization(JsonNode node) {
        return ImmutableGitOrganization.of(node.get("username").asText());
    }

    private static GitRepository toGitRepository(JsonNode node) {
        return ImmutableGitRepository.builder()
                .fullName(node.get("full_name").asText())
                .gitCloneUri(URI.create(node.get("clone_url").asText()))
                .homepage(URI.create(node.get("html_url").asText()))
                .build();
    }

    private static GitHook toGitHook(JsonNode node) {
        return ImmutableGitHook.builder()
                .name(node.get("id").asText())
                .url(node.get("config").get("url").asText())
                .events(toList(node.get("events"), JsonNode::asText))
                .build();
    }

    private static GiteaUser toGitUser(JsonNode node) {
        long id = node.get("id").longValue();
        String login = node.get("login").asText();
        String avatarUrl = node.get("avatar_url").asText();
        return new GiteaUser(id, login, avatarUrl);
    }
}