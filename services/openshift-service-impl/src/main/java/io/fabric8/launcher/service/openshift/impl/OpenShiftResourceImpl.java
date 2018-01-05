package io.fabric8.launcher.service.openshift.impl;

import java.util.Objects;

import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftResource;

/**
 *
 */
public class OpenShiftResourceImpl implements OpenShiftResource {

    /**
     * Constructor.
     *
     * @param name   the resource name
     * @param kind   the resource kind
     * @param parent the parent project
     */
    public OpenShiftResourceImpl(final String name,
                                 final String kind,
                                 final OpenShiftProject parent,
                                 final String gitHubWebhookSecret) {
        super();
        this.name = name;
        this.kind = kind;
        this.project = parent;
        this.gitHubWebhookSecret = gitHubWebhookSecret;
    }

    /**
     * the resource name.
     */
    private final String name;

    /**
     * the resource type.
     */
    private final String kind;

    /**
     * the parent project.
     */
    private final OpenShiftProject project;

    /**
     * If a BuildConfig with GitHub trigger, the gitHub webhook secret
     */
    private final String gitHubWebhookSecret;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getKind() {
        return kind;
    }

    @Override
    public OpenShiftProject getProject() {
        return project;
    }

    @Override
    public String getGitHubWebhookSecret() {
        return gitHubWebhookSecret;
    }


    @Override
    public String toString() {
        return "[" + this.kind + "] " + this.project.getName() + "." + this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenShiftResourceImpl that = (OpenShiftResourceImpl) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(kind, that.kind) &&
                Objects.equals(project, that.project) &&
                Objects.equals(gitHubWebhookSecret, that.gitHubWebhookSecret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kind, project, gitHubWebhookSecret);
    }
}
