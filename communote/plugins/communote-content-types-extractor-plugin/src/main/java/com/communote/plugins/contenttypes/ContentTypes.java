package com.communote.plugins.contenttypes;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public enum ContentTypes {
    /** Document */
    DOCUMENT,
    /** Link */
    LINK,
    /** Image */
    IMAGE,
    /** Rich media */
    RICH_MEDIA("richMedia"),
    /** Idea */
    IDEA,
    /** Question */
    QUESTION,
    /** Any Attachment. */
    ATTACHMENT;

    private String propertyKey;

    private String propertyValue;

    /**
     * Constructor
     */
    private ContentTypes() {
        this(null);
    }

    /**
     * @param value
     *            Value of the property.
     */
    private ContentTypes(String value) {
        if (value == null) {
            value = toString().toLowerCase();
        }
        propertyValue = value;
        propertyKey = "contentTypes." + value;
    }

    /**
     * @return The property key.
     */
    public String getPropertyKey() {
        return propertyKey;
    }

    /**
     * @return the propertyValue
     */
    public String getPropertyValue() {
        return propertyValue;
    }

}
