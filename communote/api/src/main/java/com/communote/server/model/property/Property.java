package com.communote.server.model.property;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class Property implements Serializable {

    public static final String KEY_GROUP_GLOBAL = "global";
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -5698205173077811425L;

    private String keyGroup;

    private String propertyKey;

    private Date lastModificationDate;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("keyGroup='");
        sb.append(keyGroup);
        sb.append("', ");

        sb.append("propertyKey='");
        sb.append(propertyKey);
        sb.append("', ");

        sb.append("lastModificationDate='");
        sb.append(lastModificationDate);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Property instance and all identifiers for
     * this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Property)) {
            return false;
        }
        final Property that = (Property) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     *
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * The group the key is belonging to
     * </p>
     */
    public String getKeyGroup() {
        return this.keyGroup;
    }

    /**
     * <p>
     * Date, which denotes the last modification of this property.
     * </p>
     */
    public Date getLastModificationDate() {
        return this.lastModificationDate;
    }

    /**
     *
     */
    public String getPropertyKey() {
        return this.propertyKey;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    /**
     * <p>
     * Return true if the keyGroup is global an key is matching.
     * </p>
     */
    public boolean keyEquals(String key) {
        return keyEquals(KEY_GROUP_GLOBAL, key);
    }

    public boolean keyEquals(String keyGroup, String key) {
        return StringUtils.equals(keyGroup, getKeyGroup())
                && StringUtils.equals(key, getPropertyKey());
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setKeyGroup(String keyGroup) {
        this.keyGroup = keyGroup;
    }

    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }
}