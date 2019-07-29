package io.fabric8.launcher.operator;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import io.fabric8.launcher.operator.cr.LauncherResource;
import io.fabric8.launcher.operator.cr.LauncherResourceDoneable;
import io.fabric8.launcher.operator.cr.LauncherResourceList;
import io.fabric8.openshift.api.model.Template;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.InputStream;

/**
 * An Operator listens for events and performs necessary work
 */
@ApplicationScoped
public class LauncherOperator implements Watcher<LauncherResource> {

    private static final String CRD_NAME = "launchers.launcher.fabric8.io";
    private static final String CRD_API_VERSION = "launcher.fabric8.io/v1beta1";
    private static final String CRD_KIND = "Launcher";

    @Inject
    KubernetesClient client;

    void onStartup(@Observes StartupEvent _ev) {
        // Register deserializers
        KubernetesDeserializer.registerCustomKind(CRD_API_VERSION, CRD_KIND, LauncherResource.class);
        // Fetch installed CRD
        CustomResourceDefinition crd = client.customResourceDefinitions().withName(CRD_NAME).require();
        // Watch for events
        client.customResources(crd, LauncherResource.class, LauncherResourceList.class, LauncherResourceDoneable.class)
                .inNamespace(client.getNamespace())
                .watch(this);
    }

    @Override
    public void eventReceived(Action action, LauncherResource resource) {
        switch (action) {
            case ADDED:
                onAdded(resource);
                break;
            case MODIFIED:
                onModified(resource);
                break;
            case DELETED:
                onDeleted(resource);
                break;
            case ERROR:
                onError(resource);
                break;
            default:
                System.exit(-1);
        }
    }

    @Override
    public void onClose(KubernetesClientException cause) {
        if (cause != null) {
            cause.printStackTrace();
        }
        System.exit(-1);
    }

    private void onAdded(LauncherResource resource) {
        System.out.println();
        System.out.println("ADDED: " + resource + "->" + Thread.currentThread().getName());
    }

    private void onDeleted(LauncherResource resource) {
        System.out.println("DELETED: " + resource + "->" + Thread.currentThread().getName());
    }

    private void onModified(LauncherResource resource) {
        System.out.println("MODIFIED: " + resource + "->" + Thread.currentThread().getName());
    }

    private void onError(LauncherResource resource) {
        System.out.println("ERROR:" + resource + "->" + Thread.currentThread().getName());
    }


    private Template readTemplate() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/launcher-template.yaml")) {
            //TODO Read template
        }
        return null;
    }


}
