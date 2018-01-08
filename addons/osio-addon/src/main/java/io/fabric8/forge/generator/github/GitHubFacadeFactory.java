package io.fabric8.forge.generator.github;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.forge.generator.AttributeMapKeys;
import io.fabric8.forge.generator.git.GitAccount;
import io.fabric8.forge.generator.keycloak.TokenHelper;
import org.jboss.forge.addon.ui.context.UIContext;

/**
 * A factory to create a GitHubFacade with.
 */
@ApplicationScoped
public class GitHubFacadeFactory {

    public GitHubFacade createGitHubFacade(UIContext context) {
        GitAccount details = (GitAccount) context.getAttributeMap().get(AttributeMapKeys.GIT_ACCOUNT);
        if (details == null) {
            String authHeader = TokenHelper.getMandatoryAuthHeader(context);
            details = GitAccount.loadFromSaaS(authHeader);
        }
        return new GitHubFacade(details);
    }
}
