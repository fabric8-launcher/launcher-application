package io.fabric8.launcher.operator.cr;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class LauncherResourceDoneable extends CustomResourceDoneable<LauncherResource> {
    public LauncherResourceDoneable(LauncherResource resource, Function<LauncherResource, LauncherResource> function) {
        super(resource, function);
    }
}
