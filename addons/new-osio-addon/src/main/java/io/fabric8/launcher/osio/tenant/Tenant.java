package io.fabric8.launcher.osio.tenant;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableTenant.class)
@JsonSerialize(as = ImmutableTenant.class)
public interface Tenant {
    String getId();

    String getType();

    String getEmail();

    List<Namespace> getNamespaces();

}
