package io.fabric8.launcher.service.github.impl.kohsuke;

import io.fabric8.launcher.service.github.api.GitHubUser;
import org.kohsuke.github.GHUser;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class KohsukeGitHubUser implements GitHubUser {

    KohsukeGitHubUser(GHUser ghUser) {
        this.ghUser = ghUser;
    }

    private final GHUser ghUser;

    @Override
    public String getLogin() {
        return ghUser.getLogin();
    }
}
