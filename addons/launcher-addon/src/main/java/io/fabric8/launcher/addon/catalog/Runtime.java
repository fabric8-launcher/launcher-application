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
public class Runtime implements Comparable<Runtime> {
   public Runtime(String id) {
      this(id, id, null);
   }

   public Runtime(String id, String name, String icon) {
        assert id != null : "Runtime Id cannot be null";
        assert name != null : "Runtime Name cannot be null";
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    private final String icon;

    private final String id;

    private final String name;

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
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    @Override
    public int compareTo(Runtime o) {
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
        Runtime other = (Runtime) obj;
        if (id == null) {
            return other.id == null;
        } else return id.equals(other.id);
    }

    @Override
    public String toString() {
        return "Runtime [id=" + id + ", name=" + name + ", icon=" + icon + "]";
    }
}