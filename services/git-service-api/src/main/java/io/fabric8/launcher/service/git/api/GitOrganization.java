package io.fabric8.launcher.service.git.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * A Git organization
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGitOrganization.class)
@JsonDeserialize(as = ImmutableGitOrganization.class)
public interface GitOrganization extends Comparable<GitOrganization> {

    @Value.Parameter
    String getName();

    @Value.Derived
    @Override
    default int compareTo(GitOrganization o) {
        return getName().compareTo(o.getName());
    }
}
