package io.openshift.appdev.missioncontrol.service.openshift.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftResource;

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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OpenShiftProjectImpl other = (OpenShiftProjectImpl) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }


}
