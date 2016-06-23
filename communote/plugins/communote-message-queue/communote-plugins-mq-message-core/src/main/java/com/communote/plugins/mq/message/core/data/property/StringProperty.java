package com.communote.plugins.mq.message.core.data.property;

/**
 * POJO representing a property that is assigned to one of the Communote entities (blog, note, user,
 * external object).
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StringProperty {

    private String group;

    private String key;

    private String value;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        StringProperty other = (StringProperty) obj;
        boolean equal = true;
        if (group == null) {
            if (other.group != null) {
                equal = false;
            }
        } else if (!group.equals(other.group)) {
            equal = false;
        }
        if (key == null) {
            if (other.key != null) {
                equal = false;
            }
        } else if (!key.equals(other.key)) {
            equal = false;
        }
        return equal;
    }

    /**
     * @return the group the property belongs to. Main idea behind this attribute is that plugins
     *         can provide their own group to avoid conflicts in the keys.
     */
    public String getGroup() {
        return group;
    }

    /**
     * @return the key of the property which uniquely identifies the property in its group. Key and
     *         group make the property globally unique.
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the value of the property, can be null to remove an existing value
     */
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    /**
     * Set the group the property belongs to.
     * 
     * @param group
     *            the group the property
     * @see #getGroup()
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Set the key of the property
     * 
     * @param key
     *            the key of the property
     * @see #getKey()
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Set the value of the property
     * 
     * @param value
     *            the value of the property, can be null to remove an existing property
     */
    public void setValue(String value) {
        this.value = value;
    }

}
