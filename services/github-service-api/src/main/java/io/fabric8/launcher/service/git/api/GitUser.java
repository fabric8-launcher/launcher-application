package io.fabric8.launcher.service.git.api;

/**
 * Value Object representing a Git user
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface GitUser {

    /**
     * @return The login for this {@link GitUser}
     */
    String getLogin();
}
