package io.fabric8.launcher.web.endpoints.models;

import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.launcher.service.git.api.GitUser;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableGitDetailedUser.class)
@JsonDeserialize(as = ImmutableGitDetailedUser.class)
public interface GitDetailedUser extends GitUser {
    Set<String> getOrganizations();
    Set<String> getRepositories();
}
