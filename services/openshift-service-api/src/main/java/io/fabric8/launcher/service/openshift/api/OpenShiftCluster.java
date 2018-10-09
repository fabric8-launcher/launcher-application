package io.fabric8.launcher.service.openshift.api;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class OpenShiftCluster {

    public OpenShiftCluster(String id, String name, String type, String apiUrl, String consoleUrl) {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(apiUrl, "apiUrl is required");
        Objects.requireNonNull(consoleUrl, "consoleUrl is required");
        this.id = id;
        this.name = name;
        this.type = type;
        this.apiUrl = apiUrl;
        this.consoleUrl = consoleUrl;
    }

    private final String id;

    private final String name;

    private final String type;

    private final String apiUrl;

    private final String consoleUrl;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @JsonIgnore
    public String getApiUrl() {
        return apiUrl;
    }

    public String getConsoleUrl() {
        return consoleUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenShiftCluster that = (OpenShiftCluster) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(apiUrl, that.apiUrl) &&
                Objects.equals(consoleUrl, that.consoleUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, apiUrl, consoleUrl);
    }

    @Override
    public String toString() {
        return "OpenShiftCluster{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", consoleUrl='" + consoleUrl + '\'' +
                '}';
    }
}
