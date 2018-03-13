package io.fabric8.launcher.service.git.api;


import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable(singleton = true)
@JsonSerialize(as = ImmutableGitRepositoryFilter.class)
@JsonDeserialize(as = ImmutableGitRepositoryFilter.class)
public interface GitRepositoryFilter {

    @Nullable String withNameContaining();

    @Nullable GitOrganization withOrganization();
}
