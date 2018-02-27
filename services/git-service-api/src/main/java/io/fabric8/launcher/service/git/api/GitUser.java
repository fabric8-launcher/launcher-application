package io.fabric8.launcher.service.git.api;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Value Object representing a Git user
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGitUser.class)
@JsonDeserialize(as = ImmutableGitUser.class)
public interface GitUser {

    /**
     * @return The login for this {@link GitUser}
     */
    @Value.Parameter
    String getLogin();

    /**
     * @return The avatar URL for this {@link GitUser}
     */
    @Value.Parameter
    @Nullable
    String getAvatarUrl();


    /**
     * @return The email for this {@link GitUser}
     */
    @Value.Parameter
    @Nullable
    String getEmail();
}
