package io.fabric8.launcher.service.git.api;

import org.immutables.value.Value;

/**
 * A Git organization
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
public interface GitOrganization {

    @Value.Parameter
    String getName();
}
