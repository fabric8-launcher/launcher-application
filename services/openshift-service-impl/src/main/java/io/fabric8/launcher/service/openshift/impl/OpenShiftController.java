package io.fabric8.launcher.service.openshift.impl;

import io.fabric8.kubernetes.api.Controller;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.client.OpenShiftClient;

/**
 * This controller has fixes for starter clusters
 *
 * eg. Starter cluster does not immediately returns that a project was created in
 * https://console.starter-us-east-1.openshift.com/apis/project.openshift.io/v1/projects for example, so the checkNamespace method
 * must act differently
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class OpenShiftController extends Controller {

    OpenShiftController(OpenShiftClient client) {
        super(client);
    }

    @Override
    public boolean checkNamespace(String namespaceName) {
        OpenShiftClient client = getOpenShiftClientOrNull();
        try {
            Project project = client.projects().withName(namespaceName).get();
            return project != null;
        } catch (KubernetesClientException ignored) {
            return false;
        }
    }
}
