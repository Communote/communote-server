package com.communote.server.api.core.property;

/**
 * Value object for a property with a string value.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StringPropertyTO implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -5559466075579216359L;

    private String propertyValue;

    private String keyGroup;

    private String propertyKey;

    private java.util.Date lastModificationDate;

    public StringPropertyTO() {
        this.propertyValue = null;
        this.keyGroup = null;
        this.propertyKey = null;
        this.lastModificationDate = null;
    }

    public StringPropertyTO(String propertyValue, String keyGroup, String propertyKey,
            java.util.Date lastModificationDate) {
        this.propertyValue = propertyValue;
        this.keyGroup = keyGroup;
        this.propertyKey = propertyKey;
        this.lastModificationDate = lastModificationDate;
    }

    /**
     * Copies constructor from other StringPropertyTO
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public StringPropertyTO(StringPropertyTO otherBean) {
        this(otherBean.getPropertyValue(), otherBean.getKeyGroup(), otherBean.getPropertyKey(),
                otherBean.getLastModificationDate());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(StringPropertyTO otherBean) {
        if (otherBean != null) {
            this.setPropertyValue(otherBean.getPropertyValue());
            this.setKeyGroup(otherBean.getKeyGroup());
            this.setPropertyKey(otherBean.getPropertyKey());
            this.setLastModificationDate(otherBean.getLastModificationDate());
        }
    }

    /**
     *
     */
    public String getKeyGroup() {
        return this.keyGroup;
    }

    /**
     *
     */
    public java.util.Date getLastModificationDate() {
        return this.lastModificationDate;
    }

    /**
     *
     */
    public String getPropertyKey() {
        return this.propertyKey;
    }

    /**
     *
     */
    public String getPropertyValue() {
        return this.propertyValue;
    }

    public void setKeyGroup(String keyGroup) {
        this.keyGroup = keyGroup;
    }

    public void setLastModificationDate(java.util.Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

}