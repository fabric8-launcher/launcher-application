package io.fabric8.launcher.osio.tenant;

import javax.ws.rs.core.HttpHeaders;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.identity.IdentityHelper;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.osio.EnvironmentVariables;
import okhttp3.Request;

import static io.fabric8.launcher.base.http.ExternalRequest.readJson;

public final class TenantRequests {

    private TenantRequests() {
        throw new IllegalAccessError("Helper class");
    }

    public static Tenant getTenant(final TokenIdentity osioToken) {
        String authorizationHeader = IdentityHelper.createRequestAuthorizationHeaderValue(osioToken);
        Request userInfoRequest = new Request.Builder()
                .url(EnvironmentVariables.ExternalServices.getTenantIdentityURL())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .build();

        Request namespacesRequest = new Request.Builder()
                .url(EnvironmentVariables.ExternalServices.getTenantNamespacesURL())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .build();

        Tenant tenant = readJson(userInfoRequest, tree -> readUserInfo(tree, osioToken))
                .map(builder -> readJson(namespacesRequest, namespaces -> addNamespaces(builder, namespaces)).get())
                .orElseThrow(() -> new BadTenantException("Tenant not found"));
        return tenant;
    }

    private static ImmutableTenant.Builder readUserInfo(JsonNode tree, TokenIdentity token) {
        JsonNode attributes = tree.get("data").get("attributes");
        return ImmutableTenant.builder()
                .identity(token)
                .email(attributes.get("email").asText())
                .username(attributes.get("username").asText());
    }

    private static Tenant addNamespaces(ImmutableTenant.Builder builder, JsonNode tree) {
        JsonNode namespaces = tree.get("data").get("attributes").get("namespaces");
        for (JsonNode namespaceJson : namespaces) {
            Tenant.Namespace namespace = ImmutableNamespace.builder()
                    .name(namespaceJson.get("name").asText())
                    .type(namespaceJson.get("type").asText())
                    .clusterUrl(namespaceJson.get("cluster-url").asText())
                    .clusterConsoleUrl(namespaceJson.get("cluster-console-url").asText())
                    .build();
            builder.addNamespace(namespace);
        }
        return builder.build();
    }
}
