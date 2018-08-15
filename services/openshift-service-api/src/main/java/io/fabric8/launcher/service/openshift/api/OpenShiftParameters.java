package io.fabric8.launcher.service.openshift.api;

import javax.annotation.Nullable;

import io.fabric8.launcher.base.identity.Identity;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Value.Immutable
public interface OpenShiftParameters {

    OpenShiftCluster getCluster();

    Identity getIdentity();

    @Nullable
    String getImpersonateUsername();
}
