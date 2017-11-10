package io.fabric8.launcher.service.github.api;

/**
 * Value Object representing a GitHub user
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface GitHubUser {

    /**
     * The login for this {@link GitHubUser}
     *
     * @return
     */
    String getLogin();
}
