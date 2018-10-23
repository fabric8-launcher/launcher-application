package io.fabric8.launcher.service.git.gitea;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.launcher.service.git.api.GitUser;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@JsonSerialize(as = GiteaUser.class)
@JsonDeserialize(as = GiteaUser.class)
public class GiteaUser implements GitUser {

    private final long id;

    private final String login;

    private final String avatarUrl;

    public GiteaUser(long id, String login, String avatarUrl) {
        this.id = id;
        this.login = login;
        this.avatarUrl = avatarUrl;
    }

    @JsonIgnore
    public long getId() {
        return id;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Nullable
    @Override
    public String getAvatarUrl() {
        return avatarUrl;
    }
}
