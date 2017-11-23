package io.fabric8.launcher.service.openshift.impl;

import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.impl.fabric8.openshift.client.Fabric8OpenShiftServiceFactory;
import io.fabric8.launcher.service.openshift.test.OpenShiftTestCredentials;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de
 * Oliveira</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
public class OpenShiftServicePojoIT extends OpenShiftServiceTestBase {

    @Override
    public OpenShiftService getOpenShiftService() {
        return new Fabric8OpenShiftServiceFactory(new OpenShiftClusterRegistryImpl()).create(OpenShiftTestCredentials.getIdentity());
    }
}
