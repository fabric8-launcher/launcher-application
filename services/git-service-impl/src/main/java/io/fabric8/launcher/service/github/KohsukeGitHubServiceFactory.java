package io.fabric8.launcher.service.github;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.launcher.base.EnvironmentSupport;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.git.api.GitServiceFactory;
import io.fabric8.launcher.service.git.spi.GitProvider;
import io.fabric8.launcher.service.github.api.GitHubEnvVarSysPropNames;
import okhttp3.OkHttpClient;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import static io.fabric8.launcher.service.git.spi.GitProvider.GitProviderType.GITHUB;
import static io.fabric8.launcher.service.github.api.GitHubEnvVarSysPropNames.LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN;

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

    private Logger log = Logger.getLogger(KohsukeGitHubServiceFactory.class.getName());

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
            final GitHubBuilder ghb = new GitHubBuilder()
                    .withConnector(new OkHttp3Connector(new OkHttpClient()));
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
            throw new RuntimeException("Could not create GitHub client", ioe);
        }
        final GitService ghs = new KohsukeGitHubService(gitHub, identity);
        log.finest(() -> "Created backing GitHub client for identity using " + identity.getClass().getSimpleName());
        return ghs;
    }

    @Override
    public Optional<Identity> getDefaultIdentity() {
        // Try using the provided Github token
        String token = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(LAUNCHER_MISSIONCONTROL_GITHUB_TOKEN);
        return Optional.ofNullable(token).map(IdentityFactory::createFromToken);
    }
}