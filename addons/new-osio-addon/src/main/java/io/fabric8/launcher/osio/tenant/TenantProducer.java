package io.fabric8.launcher.osio.tenant;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.osio.EnvironmentVariables;
import io.fabric8.launcher.osio.http.ExternalRequest;
import okhttp3.Request;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class TenantProducer {

    @Produces
    @RequestScoped
    public Tenant produceTenant(HttpServletRequest servletRequest) {
        String authorizationHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        Request request = new Request.Builder()
                .url(EnvironmentVariables.ExternalServices.getTenantServiceURL())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .build();
        Tenant tenant = ExternalRequest.readJson(request, this::readTenantData)
                .orElseThrow(() -> new BadTenantException("Tenant not found"));
        return tenant;
    }

    private Tenant readTenantData(JsonNode tree) {
        JsonNode data = tree.get("data");
        return ImmutableTenant.builder()
                .id(data.get("id").asText())
                .type(data.get("type").asText())
                .email(data.get("email").asText())
                .build();
    }


}
