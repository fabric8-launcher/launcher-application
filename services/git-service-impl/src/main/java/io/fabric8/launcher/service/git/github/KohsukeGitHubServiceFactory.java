package io.fabric8.launcher.service.git.github;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.http.HttpClient;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.github.api.GitHubEnvVarSysPropNames;
import io.fabric8.launcher.service.git.spi.GitProvider;
import org.kohsuke.github.AbuseLimitHandler;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;
import org.kohsuke.github.extras.OkHttp3Connector;

import static io.fabric8.launcher.base.EnvironmentSupport.getEnvVarOrSysProp;
import static io.fabric8.launcher.service.git.github.api.GitHubEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN;
import static io.fabric8.launcher.service.git.spi.GitProvider.GitProviderType.GITHUB;

/**
 * Implementation of the {@link GitServiceFactory}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
@GitProvider(GITHUB)
public class KohsukeGitHubServiceFactory implements GitServiceFactory {

    /**
     * Lazy initialization
     */
    private final Supplier<HttpClient> httpClient;

    /**
     * Used in tests and proxies
     */
    public KohsukeGitHubServiceFactory() {
        this.httpClient = HttpClient::create;
    }

    @Inject
    public KohsukeGitHubServiceFactory(final HttpClient httpClient) {
        this.httpClient = () -> httpClient;
    }

    @Override
    public String getName() {
        return "GitHub";
    }

    /**
     * Creates a new {@link GitService} with the default authentication.
     *
     * @return the created {@link GitService}
     */
    @Override
    public GitService create() {
        return create(getDefaultIdentity().orElseThrow(() -> new IllegalStateException("Env var " + GitHubEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN + " is not set.")));
    }

    @Override
    public GitService create(final Identity identity) {

        // Precondition checks
        if (identity == null) {
            throw new IllegalArgumentException("Identity is required");
        }

        final GitHub gitHub;
        try {
            @SuppressWarnings("deprecation") final GitHubBuilder ghb = new GitHubBuilder()
                    .withAbuseLimitHandler(AbuseLimitHandler.FAIL)
                    .withRateLimitHandler(RateLimitHandler.FAIL)
                    .withConnector(new OkHttp3Connector(new okhttp3.OkUrlFactory(httpClient.get().getClient())));
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
        } catch (final IOException ioe) {
            throw new UncheckedIOException("Could not create GitHub client", ioe);
        }
        return new KohsukeGitHubService(gitHub, identity);
    }

    @Override
    public Optional<Identity> getDefaultIdentity() {
        // Try using the provided Github token
        String token = getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN);
        return Optional.ofNullable(token).map(TokenIdentity::of);
    }
}