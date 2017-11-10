package io.fabric8.launcher.service.github.impl.kohsuke;

import org.kohsuke.github.GHUser;

import io.fabric8.launcher.service.github.api.GitHubUser;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class KohsukeGitHubUser implements GitHubUser {

    private final GHUser ghUser;

    KohsukeGitHubUser(GHUser ghUser) {
        this.ghUser = ghUser;
    }

    @Override
    public String getLogin() {
        return ghUser.getLogin();
    }
}
