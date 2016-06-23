package com.communote.server.core.property;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A property filter definition.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SimplePropertyFilterDefinition {

    private final ConcurrentMap<String, Collection<String>> propertyFilters =
            new ConcurrentHashMap<String, Collection<String>>();

    /**
     * Add a property to the property filter definition.
     * 
     * @param keyGroup
     *            the key group of the property
     * @param propertyKey
     *            the property key of the property
     */
    public synchronized void add(String keyGroup, String propertyKey) {
        Collection<String> propertyKeys = new HashSet<String>();
        if (propertyFilters.get(keyGroup) != null) {
            propertyKeys.addAll(propertyFilters.get(keyGroup));
        }
        propertyKeys.add(propertyKey);
        propertyFilters.put(keyGroup, propertyKeys);
    }

    /**
     * Test whether a key group and key combination is in the filters.
     * 
     * @param keyGroup
     *            the key group of the property
     * @param propertyKey
     *            the key of the property
     * @return true if contained, false otherwise
     */
    public boolean includes(String keyGroup, String propertyKey) {
        Collection<String> propertyKeys = propertyFilters.get(keyGroup);
        if (propertyKeys != null) {
            return propertyKeys.contains(propertyKey);
        }
        return false;
    }

    /**
     * Remove the the given property for the given key group from the filter definition.
     * 
     * @param keyGroup
     *            the key group
     * @param propertyKey
     *            the property key
     */
    public synchronized void remove(String keyGroup, String propertyKey) {
        Collection<String> propertyKeys = new HashSet<String>();
        if (propertyFilters.get(keyGroup) != null) {
            propertyKeys.addAll(propertyFilters.get(keyGroup));
        }
        propertyKeys.remove(propertyKey);
        propertyFilters.put(keyGroup, propertyKeys);
    }
}
