package io.fabric8.launcher.service.github.impl;

import java.io.IOException;

import io.fabric8.launcher.service.git.api.GitUser;
import org.kohsuke.github.GHUser;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class KohsukeGitHubUser implements GitUser {

    KohsukeGitHubUser(GHUser ghUser) {
        this.ghUser = ghUser;
    }

    private final GHUser ghUser;

    @Override
    public String getLogin() {
        return ghUser.getLogin();
    }

    @Override
    public String getEmail() {
        try {
            return ghUser.getEmail();
        } catch (IOException e) {
            throw new IllegalStateException("Could not retrieve email from current user", e);
        }
    }
}
