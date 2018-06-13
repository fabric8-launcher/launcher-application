package io.fabric8.launcher.service.openshift.api;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class OpenShiftCluster {

    public OpenShiftCluster(String id, String type, String apiUrl, String consoleUrl) {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(apiUrl, "apiUrl is required");
        Objects.requireNonNull(consoleUrl, "consoleUrl is required");
        this.id = id;
        this.type = type;
        this.apiUrl = apiUrl;
        this.consoleUrl = consoleUrl;
    }

    private final String id;

    private final String apiUrl;

    private final String consoleUrl;

    private final String type;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @JsonIgnore
    public String getApiUrl() {
        return apiUrl;
    }

    @JsonIgnore
    public String getConsoleUrl() {
        return consoleUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenShiftCluster that = (OpenShiftCluster) o;

        if (!id.equals(that.id)) return false;
        if (!apiUrl.equals(that.apiUrl)) return false;
        if (!consoleUrl.equals(that.consoleUrl)) return false;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + apiUrl.hashCode();
        result = 31 * result + consoleUrl.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "OpenShiftCluster{" +
                "id='" + id + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", consoleUrl='" + consoleUrl + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
