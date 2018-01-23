/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon.catalog;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class Mission implements Comparable<Mission> {
    public Mission(String id) {
        this(id, id, null);
    }

    public Mission(String id, String name, String description) {
        assert id != null : "Mission Id cannot be null";
        assert name != null : "Mission Name cannot be null";
        this.id = id;
        this.name = name;
        this.description = description;
    }

    private final String id;

    private final String name;

    private final String description;

    /**
     * This method is needed so the Web UI can know what's the internal ID used
     */
    public String getKey() {
        return getId();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return get description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public int compareTo(Mission o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        Mission other = (Mission) obj;
        if (id == null) {
            return other.id == null;
        } else return id.equals(other.id);
    }

    @Override
    public String toString() {
        return "Mission [id=" + id + ", name=" + name + ", description=" + description + "]";
    }
}