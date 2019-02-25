package io.fabric8.launcher.service.git.gitea;

import java.util.Optional;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceConfig;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.api.ImmutableGitServiceConfig;
import io.fabric8.launcher.service.git.spi.GitProvider;

import static io.fabric8.launcher.service.git.gitea.api.GiteaEnvironment.LAUNCHER_BACKEND_GITEA_TOKEN;
import static io.fabric8.launcher.service.git.gitea.api.GiteaEnvironment.LAUNCHER_BACKEND_GITEA_URL;
import static io.fabric8.launcher.service.git.gitea.api.GiteaEnvironment.LAUNCHER_BACKEND_GITEA_USERNAME;
import static io.fabric8.launcher.service.git.spi.GitProviderType.GITEA;
import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
@GitProvider(GITEA)
public class GiteaServiceFactory implements GitServiceFactory {

    private static final String GITEA_USERNAME = LAUNCHER_BACKEND_GITEA_USERNAME.value("admin");

    private static final GitServiceConfig DEFAULT_CONFIG = ImmutableGitServiceConfig.builder()
            .id("Gitea")
            .name("Gitea")
            .apiUrl(LAUNCHER_BACKEND_GITEA_URL.value("https://try.gitea.io"))
            .repositoryUrl(LAUNCHER_BACKEND_GITEA_URL.value("https://try.gitea.io"))
            .type(GITEA)
            .putServerProperties("adminUser", GITEA_USERNAME)
            .build();


    /**
     * Lazy initialization
     */
    private final Supplier<HttpClient> httpClient;

    /**
     * Used in tests and proxies
     */
    public GiteaServiceFactory() {
        this.httpClient = HttpClient::create;
    }

    @Inject
    public GiteaServiceFactory(HttpClient httpClient) {
        this.httpClient = () -> httpClient;
    }

    @Override
    public GitService create(Identity identity, String login, GitServiceConfig config) {
        requireNonNull(identity, "Identity is required");
        requireNonNull(login, "A logged user is required");
        requireNonNull(config, "GitProviderConfig is required");
        return new GiteaService(identity,
                                config.getApiUrl(),
                                config.getServerProperties().getOrDefault("adminUser", GITEA_USERNAME),
                                login, httpClient.get());
    }

    @Override
    public Optional<Identity> getDefaultIdentity() {
        TokenIdentity identity = null;
        String token = LAUNCHER_BACKEND_GITEA_TOKEN.value();
        if (token != null) {
            identity = TokenIdentity.of(token);
        }
        return Optional.ofNullable(identity);
    }


    @Override
    public GitServiceConfig getDefaultConfig() {
        return DEFAULT_CONFIG;
    }
}
