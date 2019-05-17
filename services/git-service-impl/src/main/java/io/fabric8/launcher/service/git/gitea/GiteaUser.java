package io.fabric8.launcher.service.git.gitea;

import java.util.Objects;

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

    @Override
    public String toString() {
        return "GiteaUser{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GiteaUser giteaUser = (GiteaUser) o;
        return id == giteaUser.id &&
                Objects.equals(login, giteaUser.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login);
    }
}
