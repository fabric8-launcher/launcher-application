package io.fabric8.launcher.service.openshift.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftResource;

/**
 * Implementation of a value object representing a project in OpenShift
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public final class OpenShiftProjectImpl implements OpenShiftProject {

    /**
     * Creates a new {@link OpenShiftProject} value object
     *
     * @param name
     * @throws IllegalArgumentException
     */
    public OpenShiftProjectImpl(final String name, final String consoleUrl) throws IllegalArgumentException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
        if (consoleUrl == null || consoleUrl.isEmpty()) {
            throw new IllegalArgumentException("consoleUrl is required");
        }
        this.name = name;
        this.consoleUrl = consoleUrl;
    }

    private static final String CONSOLE_OVERVIEW_URL_PREFIX = "/console/project/";

    private static final String CONSOLE_OVERVIEW_URL_SUFFIX = "/overview/";

    private final String name;

    private final String consoleUrl;

    private final List<OpenShiftResource> resources = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getConsoleOverviewUrl() {
        final StringBuilder sb = new StringBuilder();
        sb.append(consoleUrl);
        sb.append(CONSOLE_OVERVIEW_URL_PREFIX);
        sb.append(this.getName());
        sb.append(CONSOLE_OVERVIEW_URL_SUFFIX);
        final URL url;
        try {
            url = new URL(sb.toString());
        } catch (final MalformedURLException murle) {
            throw new RuntimeException(murle);
        }

        return url;
    }

    /**
     * Adds a existing resource on the project
     *
     * @param resource the resource to add
     */
    public void addResource(final OpenShiftResource resource) {
        this.resources.add(resource);
    }

    @Override
    public List<OpenShiftResource> getResources() {
        return Collections.unmodifiableList(this.resources);
    }

    @Override
    public String toString() {
        return "[Project] " + this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenShiftProjectImpl that = (OpenShiftProjectImpl) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
