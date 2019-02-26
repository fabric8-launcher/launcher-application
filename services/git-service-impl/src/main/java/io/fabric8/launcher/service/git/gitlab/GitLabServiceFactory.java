package io.fabric8.launcher.service.git.gitlab;

import java.util.Optional;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.git.api.GitServiceConfig;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.api.ImmutableGitServiceConfig;
import io.fabric8.launcher.service.git.spi.GitProvider;

import static io.fabric8.launcher.service.git.gitlab.api.GitLabEnvironment.LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN;
import static io.fabric8.launcher.service.git.gitlab.api.GitLabEnvironment.LAUNCHER_MISSIONCONTROL_GITLAB_URL;
import static io.fabric8.launcher.service.git.spi.GitProviderType.GITLAB;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
@GitProvider(GITLAB)
public class GitLabServiceFactory implements GitServiceFactory {

    private static final GitServiceConfig DEFAULT_CONFIG = ImmutableGitServiceConfig.builder()
            .id("GitLab")
            .name("GitLab")
            .type(GITLAB)
            .apiUrl(LAUNCHER_MISSIONCONTROL_GITLAB_URL.value("https://gitlab.com"))
            .repositoryUrl(LAUNCHER_MISSIONCONTROL_GITLAB_URL.value("https://gitlab.com"))
            .build();

    /**
     * Lazy initialization
     */
    private final Supplier<HttpClient> httpClient;

    /**
     * Used in tests and proxies
     */
    public GitLabServiceFactory() {
        this.httpClient = HttpClient::create;
    }


    @Inject
    public GitLabServiceFactory(HttpClient httpClient) {
        this.httpClient = () -> httpClient;
    }

    @Override
    public GitLabService create(Identity identity, String login, GitServiceConfig config) {
        if (!(identity instanceof TokenIdentity)) {
            throw new IllegalArgumentException("GitLabService supports only TokenIdentity. Not supported:" + identity);
        }
        return new GitLabService((TokenIdentity) identity, config.getApiUrl(), httpClient.get());
    }

    @Override
    public Optional<Identity> getDefaultIdentity() {
        // Try using the provided Gitlab token
        return Optional.ofNullable(getToken())
                .map(TokenIdentity::of);
    }

    private String getToken() {
        return LAUNCHER_MISSIONCONTROL_GITLAB_PRIVATE_TOKEN.value();
    }

    @Override
    public GitServiceConfig getDefaultConfig() {
        return DEFAULT_CONFIG;
    }
}
