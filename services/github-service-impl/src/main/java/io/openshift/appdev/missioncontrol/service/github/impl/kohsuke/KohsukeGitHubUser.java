package io.openshift.appdev.missioncontrol.service.github.impl.kohsuke;

import io.openshift.appdev.missioncontrol.service.github.api.GitHubUser;
import org.kohsuke.github.GHUser;

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
