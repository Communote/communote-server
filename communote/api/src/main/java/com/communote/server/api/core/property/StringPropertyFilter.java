package com.communote.server.api.core.property;

import com.communote.server.model.property.Propertyable;

/**
 * This filter allows fetching {@link Propertyable} entities which have a certain property or don't
 * have a specific property. By omitting the value it is also possible to filter for entities which
 * have (or don't have) properties with a given group and key but an arbitrary value.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class StringPropertyFilter {

    private boolean include = true;
    private String propertyKey;
    private String propertyKeyGroup;
    private String propertyValue;

    /**
     * @return the group the property key belongs to
     */
    public Object getKeyGroup() {
        return propertyKeyGroup;
    }

    /**
     * @return the key of the property
     */
    public Object getPropertyKey() {
        return propertyKey;
    }

    /**
     * @return the value of the property. Can be null.
     */
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * @return whether an entity only matches if it has or does not have a property with the key
     *         group, key and value. Defaults to true and thus the entity needs have the property.
     */
    public boolean isInclude() {
        return include;
    }

    /**
     * Set whether to only match an entity if it has or does not have a property with the key group,
     * key and value. The value of the property can be omitted to filter only for properties by
     * their group and key. By default the entity needs to have the property.
     *
     * @param include
     *            true to require that the property is assigned
     */
    public void setInclude(boolean include) {
        this.include = include;
    }

    /**
     * Set the group of the key of the property which should or should not be assigned to an entity.
     *
     * @param keyGroup
     *            the group
     */
    public void setKeyGroup(String keyGroup) {
        this.propertyKeyGroup = keyGroup;
    }

    /**
     * Set the key of the property which should or should not be assigned to an entity.
     *
     * @param key
     *            the property key
     */
    public void setPropertyKey(String key) {
        this.propertyKey = key;
    }

    /**
     * Set the value of the property which should or should not be assigned to an entity.
     *
     * @param value
     *            the value of the property. Can be null to allow arbitrary values and thus, filter
     *            for entities which have or don't have properties with the given group and key.
     */
    public void setPropertyValue(String value) {
        this.propertyValue = value;
    }
}
