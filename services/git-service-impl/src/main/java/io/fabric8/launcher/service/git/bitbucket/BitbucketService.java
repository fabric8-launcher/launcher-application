package io.fabric8.launcher.service.git.bitbucket;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.service.git.AbstractGitService;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitOrganization;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitRepositoryFilter;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.ImmutableGitHook;
import io.fabric8.launcher.service.git.api.ImmutableGitOrganization;
import io.fabric8.launcher.service.git.api.ImmutableGitRepository;
import io.fabric8.launcher.service.git.api.ImmutableGitUser;
import io.fabric8.launcher.service.git.api.NoSuchOrganizationException;
import io.fabric8.launcher.service.git.api.NoSuchRepositoryException;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import static io.fabric8.launcher.base.http.ExternalRequest.execute;
import static io.fabric8.launcher.base.http.ExternalRequest.executeAndParseJson;
import static io.fabric8.launcher.base.http.ExternalRequest.securedRequest;
import static io.fabric8.launcher.service.git.Gits.checkGitRepositoryFullNameArgument;
import static io.fabric8.launcher.service.git.Gits.checkGitRepositoryNameArgument;
import static io.fabric8.launcher.service.git.Gits.createGitRepositoryFullName;
import static io.fabric8.launcher.service.git.Gits.encode;
import static io.fabric8.launcher.service.git.Gits.isValidGitRepositoryFullName;
import static io.fabric8.launcher.service.git.bitbucket.api.BitbucketWebhookEvent.ISSUE_COMMENT_CREATED;
import static io.fabric8.launcher.service.git.bitbucket.api.BitbucketWebhookEvent.PULL_REQUEST_CREATED;
import static io.fabric8.launcher.service.git.bitbucket.api.BitbucketWebhookEvent.REPO_PUSH;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class BitbucketService extends AbstractGitService implements GitService {

    private static final MediaType APPLICATION_JSON = MediaType.parse("application/json");

    private static final String BITBUCKET_URL = "https://api.bitbucket.org";

    BitbucketService(final Identity identity) {
        super(identity);
    }

    @Override
    public void deleteRepository(final String repositoryFullName) throws IllegalArgumentException {
        checkGitRepositoryFullNameArgument(repositoryFullName);

        final String url = String.format("%s/2.0/repositories/%s", BITBUCKET_URL, repositoryFullName);
        final Request request = request()
                .delete()
                .url(url)
                .build();
        execute(request);
    }

    @Override
    public List<GitOrganization> getOrganizations() {
        final String url = String.format("%s/2.0/teams?pagelen=100&role=member", BITBUCKET_URL);
        final Request request = request()
                .get()
                .url(url)
                .build();
        return executeAndParseJson(request, BitbucketService::readGitOrganizations)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<GitRepository> getRepositories(final GitRepositoryFilter filter) {
        requireNonNull(filter, "filter must be specified.");

        final String owner;
        if (filter.withOrganization() != null) {
            final String organizationName = filter.withOrganization().getName();
            checkOrganizationExists(organizationName);
            owner = organizationName;
        } else {
            owner = getLoggedUser().getLogin();
        }
        final StringBuilder urlBuilder = new StringBuilder(String.format("%s/2.0/repositories/%s?pagelen=100", BITBUCKET_URL, encode(owner)));
        if (isNotEmpty(filter.withNameContaining())) {
            final String query = "name~\"" + filter.withNameContaining() + "\"";
            urlBuilder.append("&q=").append(encode(query));
        }
        final Request request = request()
                .get()
                .url(urlBuilder.toString())
                .build();
        return executeAndParseJson(request, BitbucketService::readGitRepositories)
                .orElse(Collections.emptyList());
    }

    @Override
    public GitRepository createRepository(final GitOrganization organization, final String repositoryName, final String description) throws IllegalArgumentException {
        checkGitRepositoryNameArgument(repositoryName);
        requireNonNull(description, "description must be specified.");

        final ObjectNode content = JsonNodeFactory.instance.objectNode()
                .put("scm", "git")
                .put("description", description);

        final String owner;
        if (organization != null) {
            checkOrganizationExists(organization.getName());
            owner = organization.getName();
        } else {
            owner = getLoggedUser().getLogin();
        }

        final String url = String.format("%s/2.0/repositories/%s/%s", BITBUCKET_URL, owner, repositoryName);
        final Request request = request()
                .post(RequestBody.create(APPLICATION_JSON, content.toString()))
                .url(url)
                .build();
        final GitRepository repository = executeAndParseJson(request, BitbucketService::readGitRepository)
                .orElseThrow(() -> new NoSuchRepositoryException(repositoryName));
        return waitForRepository(repository.getFullName());
    }

    @Override
    public GitRepository createRepository(final String repositoryName, final String description) throws IllegalArgumentException {
        return createRepository(null, repositoryName, description);
    }

    @Override
    public GitUser getLoggedUser() {
        final Request request = request()
                .get()
                .url(BITBUCKET_URL + "/2.0/user")
                .build();

        return executeAndParseJson(request, tree -> {
            final String userName = Optional.ofNullable(tree.get("username"))
                    .map(JsonNode::asText)
                    .orElseThrow(IllegalStateException::new);
            final String avatarUrl = Optional.ofNullable(tree.get("links"))
                    .map(t -> t.get("avatar"))
                    .map(t -> t.get("href"))
                    .map(JsonNode::asText)
                    .orElse(null);
            return ImmutableGitUser.builder()
                    .login(userName)
                    .avatarUrl(avatarUrl)
                    .build();
        }).orElseThrow(IllegalStateException::new);
    }

    @Override
    public Optional<GitRepository> getRepository(final String name) {
        requireNonNull(name, "name must be specified.");
        if (name.isEmpty()) {
            throw new IllegalArgumentException("repositoryName must not be empty.");
        }

        if (isValidGitRepositoryFullName(name)) {
            return getRepositoryByFullName(name);
        } else {
            checkGitRepositoryNameArgument(name);
            return getRepositoryByFullName(createGitRepositoryFullName(getLoggedUser().getLogin(), name));
        }
    }

    @Override
    public Optional<GitRepository> getRepository(final GitOrganization organization, final String repositoryName) {
        requireNonNull(organization, "organization must be specified.");
        checkGitRepositoryNameArgument(repositoryName);

        checkOrganizationExists(organization.getName());

        return getRepositoryByFullName(createGitRepositoryFullName(organization.getName(), repositoryName));
    }

    private Optional<GitRepository> getRepositoryByFullName(final String repositoryFullName) {
        checkGitRepositoryFullNameArgument(repositoryFullName);

        final String url = String.format("%s/2.0/repositories/%s", BITBUCKET_URL, repositoryFullName);
        final Request request = request()
                .get()
                .url(url)
                .build();
        return executeAndParseJson(request, BitbucketService::readGitRepository);
    }

    @Override
    public GitHook createHook(final GitRepository repository, @Nullable final String secret, final URL webhookUrl, final String... events) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(webhookUrl, "webhookUrl must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());

        final String[] effectiveEvents = events != null && events.length > 0 ? events : getSuggestedNewHookEvents();
        final ArrayNode eventsNode = JsonNodeFactory.instance.arrayNode().addAll(Stream.of(effectiveEvents).map(JsonNodeFactory.instance::textNode).collect(toList()));
        final JsonNode content = JsonNodeFactory.instance.objectNode()
                .put("url", webhookUrl.toString())
                .put("active", true)
                .set("events", eventsNode);
        final String url = String.format("%s/2.0/repositories/%s/hooks", BITBUCKET_URL, repository.getFullName());
        final Request request = request()
                .post(RequestBody.create(APPLICATION_JSON, content.toString()))
                .url(url)
                .build();
        return executeAndParseJson(request, BitbucketService::readGitHook)
                .orElse(null);
    }

    @Override
    public List<GitHook> getHooks(final GitRepository repository) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());

        final String url = String.format("%s/2.0/repositories/%s/hooks?pagelen=100", BITBUCKET_URL, repository.getFullName());
        final Request request = request()
                .get()
                .url(url)
                .build();
        return executeAndParseJson(request, BitbucketService::readGitHooks)
                .orElse(Collections.emptyList());
    }

    @Override
    public Optional<GitHook> getHook(final GitRepository repository, final URL url) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(url, "url must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());

        return getHooks(repository).stream()
                .filter(h -> h.getUrl().equalsIgnoreCase(url.toString()))
                .findFirst();
    }

    @Override
    public void deleteWebhook(final GitRepository repository, final GitHook webhook) throws IllegalArgumentException {
        requireNonNull(repository, "repository must not be null.");
        requireNonNull(webhook, "webhook must not be null.");
        checkGitRepositoryFullNameArgument(repository.getFullName());

        final String url = String.format("%s/2.0/repositories/%s/hooks/%s", BITBUCKET_URL, repository.getFullName(), encode(webhook.getName()));
        final Request request = request()
                .delete()
                .url(url)
                .build();
        execute(request);
    }

    @Override
    public String[] getSuggestedNewHookEvents() {
        return new String[]{
                REPO_PUSH.id(),
                PULL_REQUEST_CREATED.id(),
                ISSUE_COMMENT_CREATED.id()
        };
    }

    private GitOrganization checkOrganizationExists(final String name) {
        requireNonNull(name, "name must be specified.");

        final String url = String.format("%s/2.0/teams/%s", BITBUCKET_URL, encode(name));
        final Request request = request()
                .get()
                .url(url)
                .build();
        return executeAndParseJson(request, BitbucketService::readGitOrganization)
                .orElseThrow(() -> new NoSuchOrganizationException("User does not belong to organization '" + name + "' or the organization does not exist"));
    }

    private Request.Builder request() {
        return securedRequest(getIdentity());
    }

    private static List<GitRepository> readGitRepositories(final JsonNode node) {
        return streamNode(node.get("values"))
                .map(BitbucketService::readGitRepository)
                .collect(toList());
    }

    private static GitRepository readGitRepository(JsonNode node) {
        return ImmutableGitRepository.builder()
                .fullName(node.get("full_name").asText())
                .homepage(URI.create(node.get("links").get("html").get("href").asText()))
                .gitCloneUri(URI.create(node.get("links").get("clone").get(0).get("href").asText()))
                .build();
    }


    private static List<GitOrganization> readGitOrganizations(final JsonNode jsonNode) {
        return streamNode(jsonNode.get("values"))
                .map(BitbucketService::readGitOrganization)
                .sorted()
                .collect(toList());
    }

    private static Stream<JsonNode> streamNode(final JsonNode jsonNode) {
        return Optional.ofNullable(jsonNode)
                .map(JsonNode::spliterator)
                .map(s -> StreamSupport.stream(s, false))
                .orElse(Stream.empty());
    }

    private static GitOrganization readGitOrganization(final JsonNode jsonNode) {
        return ImmutableGitOrganization.builder()
                .name(jsonNode.get("username").asText())
                .build();
    }

    private static List<GitHook> readGitHooks(final JsonNode node) {
        return streamNode(node.get("values"))
                .map(BitbucketService::readGitHook)
                .collect(toList());
    }

    private static GitHook readGitHook(final JsonNode jsonNode) {
        final List<String> events = streamNode(jsonNode.get("events"))
                .map(JsonNode::asText)
                .collect(toList());
        return ImmutableGitHook.builder()
                .name(jsonNode.get("uuid").asText())
                .url(jsonNode.get("url").asText())
                .addAllEvents(events)
                .build();
    }

}
