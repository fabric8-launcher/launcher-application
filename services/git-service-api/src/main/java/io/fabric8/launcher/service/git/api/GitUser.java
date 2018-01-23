package io.fabric8.launcher.service.git.api;

import org.immutables.value.Value;

/**
 * Value Object representing a Git user
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
public interface GitUser {

    /**
     * @return The login for this {@link GitUser}
     */
    @Value.Parameter
    String getLogin();

    @Value.Parameter
    String getEmail();
}
