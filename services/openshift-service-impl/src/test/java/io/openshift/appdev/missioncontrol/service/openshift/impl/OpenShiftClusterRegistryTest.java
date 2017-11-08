package io.openshift.appdev.missioncontrol.service.openshift.impl;

import io.openshift.appdev.missioncontrol.base.test.EnvironmentVariableController;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftCluster;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftClusterRegistry;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftEnvVarSysPropNames;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class OpenShiftClusterRegistryTest {

    private OpenShiftClusterRegistry registry;

    @Before
    public void setUp() {
        System.setProperty(OpenShiftEnvVarSysPropNames.OPENSHIFT_CLUSTERS_CONFIG_FILE, "src/test/resources/openshift-clusters.yaml");
        EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.OPENSHIFT_API_URL, null);
        EnvironmentVariableController.setEnv(OpenShiftEnvVarSysPropNames.OPENSHIFT_CONSOLE_URL, null);
        registry = new OpenShiftClusterRegistryImpl();
    }

    @Test
    public void testDefaultClusterNeverNull() {
        assertThat(registry.getDefault()).isNotNull();
    }

    @Test
    public void testGetClustersIncludeDefaultCluster() {
        assertThat(registry.getClusters()).contains(registry.getDefault());
    }

    @Test
    public void testGetClustersHasAtLeastTwoItems() {
        assertThat(registry.getClusters().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void testFindNullReturnsDefault() {
        assertThat(registry.findClusterById(null).get()).isSameAs(registry.getDefault());
    }

    @Test
    public void testFindOpenshiftOnlineInt() {
        assertThat(registry.findClusterById("openshift-online-int").get())
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", "openshift-online-int")
                .hasFieldOrPropertyWithValue("apiUrl", "https://api.online-int.openshift.com/")
                .hasFieldOrPropertyWithValue("consoleUrl", "https://console.online-int.openshift.com/console");
    }

    @After
    public void tearDown() {
        System.getProperties().remove(OpenShiftEnvVarSysPropNames.OPENSHIFT_CLUSTERS_CONFIG_FILE);
    }
}