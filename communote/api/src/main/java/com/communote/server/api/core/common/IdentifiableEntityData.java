package com.communote.server.api.core.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Base value object for all entities with an ID.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class IdentifiableEntityData implements java.io.Serializable {
    private static final long serialVersionUID = -2504540245808404463L;

    private final Map<String, Object> properties = new HashMap<String, Object>();

    private Long id;

    /**
     * @param data
     *            The item to copy the data from.
     */
    public void copy(IdentifiableEntityData data) {
        if (data != null) {
            id = data.getId();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IdentifiableEntityData other = (IdentifiableEntityData) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /**
     * @return the id of the entity represented by this instance
     */
    public Long getId() {
        return id;
    }

    /**
     * @return The map of properties.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * @param <T>
     *            Type of the object.
     * @param key
     *            The key.
     * @return The object of type T or null.
     */
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Stores or replaces a value for a property.
     *
     * @param <T>
     *            Type of the value.
     * @param key
     *            Key of the property.
     * @param value
     *            Value of the property.
     */
    public <T> void setProperty(String key, T value) {
        properties.put(key, value);
    }
}