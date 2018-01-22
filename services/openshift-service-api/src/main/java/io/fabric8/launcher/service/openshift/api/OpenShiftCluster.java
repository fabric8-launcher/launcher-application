package io.fabric8.launcher.service.openshift.api;

import java.util.Objects;
import java.util.Optional;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class OpenShiftCluster {

    public OpenShiftCluster(String id, String type, String apiUrl, String consoleUrl) {
        assert id != null : "id is required";
        assert apiUrl != null : "apiUrl is required";
        assert consoleUrl != null : "consoleUrl is required";
        this.id = id;
        this.type = Optional.ofNullable(type);
        this.apiUrl = apiUrl;
        this.consoleUrl = consoleUrl;
    }

    private final String id;

    private final Optional<String> type;

    private final String apiUrl;

    private final String consoleUrl;

    public String getId() {
        return id;
    }

    public Optional<String> getType() {
        return type;
    }

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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OpenShiftCluster{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", apiUrl='" + apiUrl + '\'' +
                ", consoleUrl='" + consoleUrl + '\'' +
                '}';
    }
}
