package io.fabric8.launcher.operator.cr;

import io.fabric8.kubernetes.client.CustomResource;

public class LauncherResource extends CustomResource {
    private LauncherResourceSpec spec;

    public LauncherResourceSpec getSpec() {
        return spec;
    }

    public void setSpec(LauncherResourceSpec spec) {
        this.spec = spec;
    }

    @Override
    public String toString() {
        String name = getMetadata() != null ? getMetadata().getName() : "unknown";
        String version = getMetadata() != null ? getMetadata().getResourceVersion() : "unknown";
        return "name=" + name + " version=" + version + " value=" + spec;
    }

}
