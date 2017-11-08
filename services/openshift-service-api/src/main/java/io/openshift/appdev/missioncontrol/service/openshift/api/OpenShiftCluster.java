package io.openshift.appdev.missioncontrol.service.openshift.api;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class OpenShiftCluster {

    private final String id;

    private final String apiUrl;

    private final String consoleUrl;

    public OpenShiftCluster(String id, String apiUrl, String consoleUrl) {
        assert id != null : "id is required";
        assert apiUrl != null : "apiUrl is required";
        assert consoleUrl != null : "consoleUrl is required";
        this.id = id;
        this.apiUrl = apiUrl;
        this.consoleUrl = consoleUrl;
    }

    public String getId() {
        return id;
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

        if (!id.equals(that.id)) return false;
        if (!apiUrl.equals(that.apiUrl)) return false;
        return consoleUrl.equals(that.consoleUrl);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + apiUrl.hashCode();
        result = 31 * result + consoleUrl.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "OpenShiftCluster{" +
                "id='" + id + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", consoleUrl='" + consoleUrl + '\'' +
                '}';
    }
}
