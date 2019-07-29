package io.fabric8.launcher.operator.cr;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RegisterForReflection
public class LauncherResourceSpec {

    @JsonProperty
    public String environment = "production";

    @JsonProperty
    public OpenShift openshift;

    @JsonProperty
    public Git git;

    @JsonProperty
    public BoosterCatalog catalog;

    @JsonProperty
    public Keycloak keycloak;

    @RegisterForReflection
    public static class OpenShift {

        public String consoleUrl;

        public String clientId = "launcher";

        public String apiUrl = "htps://openshift.default.svc.cluster.local";

        public boolean impersonate;

        public String username;

        public String password;

        public String token;
    }

    @RegisterForReflection
    public static class Git {
        public List<GitProvider> providers = new ArrayList<>();
        public String username;
        public String password;
        public String token;
    }

    @RegisterForReflection
    public static class BoosterCatalog {
        public String repositoryUrl = "https://github.com/fabric8-launcher/launcher-booster-catalog";

        public String repositoryRef = "latest";

        public String filter;
    }

    @RegisterForReflection
    public static class Keycloak {
        public String url;

        public String realm;

        public String clientId;
    }

    @RegisterForReflection
    public static class GitProvider {
        public String id;
        public String name;
        public String apiUrl;
        public String repositoryUrl;
        public String type;
        public Map<String, String> clientProperties = new HashMap<>();
        public Map<String, String> serverProperties = new HashMap<>();
    }
}
