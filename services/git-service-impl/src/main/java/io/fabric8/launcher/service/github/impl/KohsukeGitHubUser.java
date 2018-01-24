package io.fabric8.launcher.service.github.impl;

import java.io.IOException;
import java.util.Optional;

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
    public Optional<String> getEmail() {
        try {
            return Optional.ofNullable(ghUser.getEmail());
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
