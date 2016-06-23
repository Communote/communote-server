package com.communote.server.core.property;

/**
 * The property accessor filter definition.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PropertyAccessorFilterDefinition {

    /* Filter definitions containing the properties that are allowed to be retrieved. */
    private final SimplePropertyFilterDefinition read = new SimplePropertyFilterDefinition();

    /* Filter definitions containing the properties that are allowed to be stored. */
    private final SimplePropertyFilterDefinition write = new SimplePropertyFilterDefinition();

    /**
     * Add a property to the read property filter definition.
     * 
     * @param keyGroup
     *            the key group of the property
     * @param propertyKey
     *            the property key of the property
     */
    public void addPropertyToReadFilterDefinition(String keyGroup, String propertyKey) {
        read.add(keyGroup, propertyKey);
    }

    /**
     * Add a property to the write property filter definition.
     * 
     * @param keyGroup
     *            the key group of the property
     * @param propertyKey
     *            the property key of the property
     */
    public void addPropertyToWriteFilterDefinition(String keyGroup, String propertyKey) {
        write.add(keyGroup, propertyKey);
    }

    /**
     * Test whether a property key group and key combination can be read.
     * 
     * @param keyGroup
     *            the key group of the property
     * @param propertyKey
     *            the property key of the property
     * @return true if the group and key combination is in the read filters, false otherwise
     */
    public boolean isPropertyAllowedToGet(String keyGroup, String propertyKey) {
        return read.includes(keyGroup, propertyKey);
    }

    /**
     * Test whether a property key group and key combination can be stored.
     * 
     * @param keyGroup
     *            the key group of the property
     * @param propertyKey
     *            the property key of the property
     * @return true if the group and key combination is in the write filters, false otherwise
     */
    public boolean isPropertyAllowedToSet(String keyGroup, String propertyKey) {
        return write.includes(keyGroup, propertyKey);
    }

    /**
     * Remove the the given property for the given key group from the read filter definition.
     * 
     * @param keyGroup
     *            the key group
     * @param propertyKey
     *            the property key
     */
    public void removePropertyFromReadFilterDefinition(String keyGroup, String propertyKey) {
        read.remove(keyGroup, propertyKey);
    }

    /**
     * Remove the the given property for the given key group from the write filter definition.
     * 
     * @param keyGroup
     *            the key group
     * @param propertyKey
     *            the property key
     */
    public void removePropertyFromWriteFilterDefinition(String keyGroup, String propertyKey) {
        write.remove(keyGroup, propertyKey);
    }
}
