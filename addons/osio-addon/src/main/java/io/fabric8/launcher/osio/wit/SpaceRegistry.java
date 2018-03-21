package io.fabric8.launcher.osio.wit;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.http.ExternalRequest;
import io.fabric8.launcher.osio.EnvironmentVariables;
import io.fabric8.launcher.osio.tenant.Tenant;
import okhttp3.Request;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Dependent
public class SpaceRegistry {

    @Inject
    private Tenant tenant;

    public Space findSpaceByID(String id) {
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + tenant.getIdentity().getToken())
                .url(EnvironmentVariables.ExternalServices.getSpaceByIDURL(id))
                .build();
        return ExternalRequest.readJson(request, tree -> {
            JsonNode data = tree.get("data");
            JsonNode attributes = data.get("attributes");
            return ImmutableSpace.builder()
                    .id(data.get("id").textValue())
                    .name(attributes.get("name").textValue())
                    .build();
        }).orElseThrow(() -> new IllegalArgumentException("Space ID not found:" + id));
    }
}
