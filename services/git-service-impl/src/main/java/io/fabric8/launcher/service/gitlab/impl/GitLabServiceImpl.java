package io.fabric8.launcher.service.gitlab.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.ImmutableGitRepository;
import io.fabric8.launcher.service.git.api.ImmutableGitUser;
import io.fabric8.launcher.service.git.api.NoSuchRepositoryException;
import io.fabric8.launcher.service.git.impl.AbstractGitService;
import io.fabric8.launcher.service.gitlab.api.GitLabService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class GitLabServiceImpl extends AbstractGitService implements GitLabService {

    private final String token;

    private static final MediaType APPLICATION_FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded");

    private static final ObjectMapper mapper = new ObjectMapper();

    GitLabServiceImpl(Identity identity) {
        super(identity);
        if (identity instanceof TokenIdentity) {
            this.token = ((TokenIdentity) identity).getToken();
        } else {
            throw new IllegalArgumentException("Unsupported identity type: " + identity);
        }
    }

    @Override
    public GitRepository createRepository(String repositoryName, String description) throws IllegalArgumentException {
        StringBuilder content = new StringBuilder();
        content.append("name=").append(repositoryName);
        if (description != null && !description.isEmpty()) {
            content.append("&description=").append(description);
        }
        Request request = request()
                .url("https://gitlab.com/api/v4/projects")
                .post(RequestBody.create(APPLICATION_FORM_URLENCODED, content.toString()))
                .build();
        return execute(request, this::readGitRepository)
                .orElseThrow(() -> new NoSuchRepositoryException(repositoryName));
    }

    @Override
    public Optional<GitRepository> getRepository(String repositoryName) {
        // Precondition checks
        if (repositoryName == null || repositoryName.isEmpty()) {
            throw new IllegalArgumentException("repository name must be specified");
        }
        if (repositoryName.contains("/")) {
            String[] split = repositoryName.split("/");
            return getRepository(split[0], split[1]);
        } else {
            return getRepository(getLoggedUser().getLogin(), repositoryName);
        }
    }

    private GitRepository readGitRepository(JsonNode node) {
        return ImmutableGitRepository.builder()
                .fullName(node.get("path_with_namespace").textValue())
                .homepage(URI.create(node.get("web_url").textValue()))
                .gitCloneUri(URI.create(node.get("http_url_to_repo").textValue()))
                .build();
    }

    @Override
    public Optional<GitRepository> getRepository(String organization, String repositoryName) {
        Request request = request().get().url("https://gitlab.com/api/v4/projects?membership=true&search=" + repositoryName).build();
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
    public GitHook createHook(GitRepository repository, URL webhookUrl, String... events) throws IllegalArgumentException {
        return null;
    }

    @Override
    public GitUser getLoggedUser() {
        Request request = request().get().url("https://gitlab.com/api/v4/user").build();
        return execute(request, tree -> ImmutableGitUser.of(tree.get("username").textValue())).orElseThrow(IllegalStateException::new);
    }

    private Request.Builder request() {
        return new Request.Builder().header("Private-Token", token);
    }

    private <T> Optional<T> execute(Request request, Function<JsonNode, T> consumer) {
        OkHttpClient httpClient = new OkHttpClient();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body == null) {
                    return Optional.empty();
                }
                JsonNode tree = mapper.readTree(body.string());
                return Optional.ofNullable(consumer.apply(tree));
            } else {
                throw new IllegalStateException(response.message());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}