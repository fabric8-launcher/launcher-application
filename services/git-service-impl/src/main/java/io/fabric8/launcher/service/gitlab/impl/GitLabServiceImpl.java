package io.fabric8.launcher.service.gitlab.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.git.api.GitHook;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitUser;
import io.fabric8.launcher.service.git.api.ImmutableGitHook;
import io.fabric8.launcher.service.git.api.ImmutableGitRepository;
import io.fabric8.launcher.service.git.api.ImmutableGitUser;
import io.fabric8.launcher.service.git.api.NoSuchRepositoryException;
import io.fabric8.launcher.service.git.impl.AbstractGitService;
import io.fabric8.launcher.service.gitlab.api.GitLabEnvVarSysPropNames;
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

    private static final MediaType APPLICATION_FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded");

    private static final String GITLAB_URL = EnvironmentSupport.INSTANCE
            .getEnvVarOrSysProp(GitLabEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITLAB_URL, "https://gitlab.com");

    private static final ObjectMapper mapper = new ObjectMapper();

    GitLabServiceImpl(TokenIdentity identity) {
        super(identity);
    }

    @Override
    public GitRepository createRepository(String repositoryName, String description) throws IllegalArgumentException {
        StringBuilder content = new StringBuilder();
        content.append("name=").append(repositoryName);
        if (description != null && !description.isEmpty()) {
            content.append("&description=").append(description);
        }
        Request request = request()
                .post(RequestBody.create(APPLICATION_FORM_URLENCODED, content.toString()))
                .url(GITLAB_URL + "/api/v4/projects")
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

    @Override
    public void deleteRepository(String repositoryName) throws IllegalArgumentException {
        Request request = request()
                .delete()
                .url(GITLAB_URL + "/api/v4/projects/" + encode(repositoryName))
                .build();
        execute(request, null);
    }

    @Override
    public GitHook createHook(GitRepository repository, URL webhookUrl, String... events) throws IllegalArgumentException {
        StringBuilder content = new StringBuilder();
        content.append("url=").append(webhookUrl);
        for (String event : events) {
            content.append("&" + event.toLowerCase() + "_events=true");
        }
        Request request = request()
                .post(RequestBody.create(APPLICATION_FORM_URLENCODED, content.toString()))
                .url(GITLAB_URL + "/api/v4/projects/" + encode(repository.getFullName()) + "/hooks")
                .build();

        return execute(request, this::readHook).orElse(null);
    }


    @Override
    public Optional<GitHook> getWebhook(GitRepository repository, URL url) throws IllegalArgumentException {
        Request request = request()
                .get()
                .url(GITLAB_URL + "/api/v4/projects/" + encode(repository.getFullName()) + "/hooks")
                .build();
        String urlAsString = url.toString();
        return execute(request, (JsonNode tree) -> {
            for (JsonNode node : tree) {
                if (urlAsString.equalsIgnoreCase(node.get("url").asText())) {
                    return readHook(node);
                }
            }
            return null;
        });
    }

    @Override
    public void deleteWebhook(GitRepository repository, GitHook webhook) throws IllegalArgumentException {
        Request request = request()
                .delete()
                .url(GITLAB_URL + "/api/v4/projects/" + encode(repository.getFullName()) + "/hooks/" + webhook.getName())
                .build();
        execute(request, null);


    }

    @Override
    public Optional<GitRepository> getRepository(String organization, String repositoryName) {
        Request request = request()
                .get()
                .url(GITLAB_URL + "/api/v4/projects?membership=true&search=" + encode(repositoryName))
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
    public GitUser getLoggedUser() {
        Request request = request()
                .get()
                .url(GITLAB_URL + "/api/v4/user")
                .build();
        return execute(request, tree -> ImmutableGitUser.of(tree.get("username").textValue())).orElseThrow(IllegalStateException::new);
    }

    private Request.Builder request() {
        TokenIdentity tokenIdentity = (TokenIdentity) identity;
        return new Request.Builder().header(tokenIdentity.getType().orElse("Authorization"), tokenIdentity.getToken());
    }

    private <T> Optional<T> execute(Request request, Function<JsonNode, T> consumer) {
        OkHttpClient httpClient = new OkHttpClient();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body == null || consumer == null) {
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

    private String encode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private ImmutableGitHook readHook(JsonNode tree) {
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

    private GitRepository readGitRepository(JsonNode node) {
        return ImmutableGitRepository.builder()
                .fullName(node.get("path_with_namespace").asText())
                .homepage(URI.create(node.get("web_url").asText()))
                .gitCloneUri(URI.create(node.get("http_url_to_repo").asText()))
                .build();
    }

}