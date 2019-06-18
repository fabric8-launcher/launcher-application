package io.fabric8.launcher.service.git.github;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.launcher.service.git.api.AuthenticationFailedException;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceConfig;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.api.ImmutableGitServiceConfig;
import io.fabric8.launcher.service.git.github.api.GitHubEnvironment;
import io.fabric8.launcher.service.git.spi.GitProvider;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.AbuseLimitHandler;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;
import org.kohsuke.github.extras.OkHttp3Connector;

import static io.fabric8.launcher.service.git.github.api.GitHubEnvironment.LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN;
import static io.fabric8.launcher.service.git.spi.GitProviderType.GITHUB;

/**
 * Implementation of the {@link GitServiceFactory}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
@GitProvider(GITHUB)
public class GitHubServiceFactory implements GitServiceFactory {

    private static final Logger log = Logger.getLogger(GitHubServiceFactory.class.getName());

    private static final GitServiceConfig DEFAULT_CONFIG = ImmutableGitServiceConfig.builder()
            .id("GitHub")
            .name("GitHub")
            .apiUrl("https://api.github.com")
            .repositoryUrl("https://github.com")
            .type(GITHUB)
            .putServerProperties("oauthUrl", "https://github.com/login/oauth/access_token")
            .build();

    /**
     * Lazy initialization
     */
    private final Supplier<HttpClient> httpClient;

    /**
     * Used in tests and proxies
     */
    public GitHubServiceFactory() {
        this.httpClient = HttpClient::create;
    }

    @Inject
    public GitHubServiceFactory(final HttpClient httpClient) {
        this.httpClient = () -> httpClient;
    }

    /**
     * Creates a new {@link GitService} with the default authentication.
     *
     * @return the created {@link GitService}
     */
    @Override
    public GitService create() {
        return create(getDefaultIdentity().orElseThrow(() -> new IllegalStateException("Env var " + GitHubEnvironment.LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN + " is not set.")),
                      null);
    }

    @Override
    public GitService create(final Identity identity, String login, GitServiceConfig config) {
        // Precondition checks
        if (identity == null) {
            throw new IllegalArgumentException("Identity is required");
        }

        final GitHub gitHub;
        try {
            // Disable Cache completely when accessing Github
            OkHttpClient client = httpClient.get().getClient()
                    .newBuilder().cache(null).build();
            @SuppressWarnings("deprecation") final GitHubBuilder ghb = new GitHubBuilder()
                    .withEndpoint(config.getApiUrl())
                    .withAbuseLimitHandler(AbuseLimitHandler.FAIL)
                    .withRateLimitHandler(RateLimitHandler.FAIL)
                    .withConnector(new OkHttp3Connector(new okhttp3.OkUrlFactory(client)));
            identity.accept(new IdentityVisitor() {
                @Override
                public void visit(TokenIdentity token) {
                    ghb.withOAuthToken(token.getToken());
                }

                @Override
                public void visit(UserPasswordIdentity userPassword) {
                    ghb.withPassword(userPassword.getUsername(), userPassword.getPassword());
                }
            });
            gitHub = ghb.build();
        } catch (final IOException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Bad credentials")) {
                throw new AuthenticationFailedException("Error while authenticating in Github", e);
            }
            // Try to grab the original error
            try {
                errorMessage = JsonUtils.readTree(errorMessage).get("message").asText();
            } catch (Exception parseError) {
                log.log(Level.FINE, "Error while parsing the error message", parseError);
            }
            if (StringUtils.isNotBlank(errorMessage)) {
                errorMessage = "Server returned: " + errorMessage;
            }
            throw new UncheckedIOException("Could not connect to GitHub. " + errorMessage, e);
        }
        return new GitHubService(gitHub, identity);
    }

    @Override
    public Optional<Identity> getDefaultIdentity() {
        // Try using the provided Github token
        String token = LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN.value();
        return Optional.ofNullable(token).map(TokenIdentity::of);
    }

    @Override
    public GitServiceConfig getDefaultConfig() {
        return DEFAULT_CONFIG;
    }
}