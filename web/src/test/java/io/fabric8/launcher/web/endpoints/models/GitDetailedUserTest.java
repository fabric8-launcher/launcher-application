package io.fabric8.launcher.web.endpoints.models;

import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.service.git.api.ImmutableGitUser;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class GitDetailedUserTest {

    @Test
    public void should_deserialize_unwrapped_user() throws Exception {
        ImmutableGitDetailedUser user = ImmutableGitDetailedUser.builder()
                .user(ImmutableGitUser.of("login", null))
                .addOrganizations("Org")
                .addRepositories("Repo")
                .build();
        String value = JsonUtils.toString(user);
        String expected = "{\"login\":\"login\",\"avatarUrl\":null,\"organizations\":[\"Org\"],\"repositories\":[\"Repo\"]}";
        assertThat(value).isEqualTo(expected);
    }

}