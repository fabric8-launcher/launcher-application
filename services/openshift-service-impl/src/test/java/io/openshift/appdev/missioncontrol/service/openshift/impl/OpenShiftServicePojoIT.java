package io.openshift.appdev.missioncontrol.service.openshift.impl;

import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.impl.fabric8.openshift.client.Fabric8OpenShiftServiceFactory;
import io.openshift.appdev.missioncontrol.service.openshift.test.OpenShiftTestCredentials;

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
