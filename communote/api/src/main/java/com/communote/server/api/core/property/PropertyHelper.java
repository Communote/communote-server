package com.communote.server.api.core.property;


/**
 * Helper for working with Property entities and their TOs.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class PropertyHelper {

    /**
     * Test whether the provided collection contains the given property and has the provided value
     * 
     * @param properties
     *            the properties to search
     * @param group
     *            the group key of the property
     * @param key
     *            the key of the property
     * @param value
     *            the value the property should have
     * @return true if the property is contained and has the value
     */
    public static boolean containsProperty(Iterable<StringPropertyTO> properties, String group,
            String key, String value) {
        StringPropertyTO property = getProperty(properties, group, key);
        if (property != null && value.equals(property.getPropertyValue())) {
            return true;
        }
        return false;
    }

    /**
     * Retrieve the identified property from the provided collection or null if it is not contained
     *
     * @param properties
     *            the properties to search
     * @param group
     *            the group key of the property
     * @param key
     *            the key of the property
     * @return the found property or null
     */
    public static StringPropertyTO getProperty(Iterable<StringPropertyTO> properties, String group,
            String key) {
        if (properties != null) {
            for (StringPropertyTO property : properties) {
                if (property.getKeyGroup().equals(group) && property.getPropertyKey().equals(key)) {
                    return property;
                }
            }
        }
        return null;
    }

    /**
     * helper
     */
    private PropertyHelper() {
        // helper constructor
    }
}
