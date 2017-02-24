package com.communote.server.api.core.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.communote.common.util.Pair;
import com.communote.server.model.property.StringProperty;

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
    public static boolean containsPropertyTO(Iterable<StringPropertyTO> properties, String group,
            String key, String value) {
        StringPropertyTO property = getPropertyTO(properties, group, key);
        if (property != null && value.equals(property.getPropertyValue())) {
            return true;
        }
        return false;
    }

    private static Set<Pair<String, String>> extractGroupKeyPairs(
            Collection<StringPropertyTO> properties) {
        Set<Pair<String, String>> result = new HashSet<>(properties.size());
        for (StringPropertyTO property : properties) {
            result.add(new Pair<>(property.getKeyGroup(), property.getPropertyKey()));
        }
        return result;
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
    public static StringPropertyTO getPropertyTO(Iterable<StringPropertyTO> properties,
            String group, String key) {
        if (properties != null) {
            for (StringPropertyTO property : properties) {
                if (property.getKeyGroup() != null && property.getKeyGroup().equals(group)
                        && property.getPropertyKey() != null
                        && property.getPropertyKey().equals(key)) {
                    return property;
                }
            }
        }
        return null;
    }

    /**
     * Add for each property of the source collection a property with same group and key and
     * <code>null</code> as value to the target collection if there is no property with that group
     * and key in the target collection.
     *
     * @param source
     *            the properties that should be nullified if they are not in the target collection.
     *            Can be null.
     * @param target
     *            the collection of properties to update. Can be null.
     * @return the updated target collection or a new collection if target was null
     */
    public static <T extends StringProperty> Collection<StringPropertyTO> nullifyMissingProperties(
            Iterable<T> source, Collection<StringPropertyTO> target) {
        if (target == null) {
            target = new ArrayList<StringPropertyTO>();
        }
        if (source != null) {
            Set<Pair<String, String>> existingGroupKeyPairs = extractGroupKeyPairs(target);
            for (T property : source) {
                if (existingGroupKeyPairs.isEmpty()
                        || !existingGroupKeyPairs.contains(new Pair<String, String>(property
                                .getKeyGroup(), property.getPropertyKey()))) {
                    target.add(new StringPropertyTO(null, property.getKeyGroup(), property
                            .getPropertyKey(), null));
                }
            }
        }
        return target;
    }

    /**
     * Add for each property of the source collection a property with same group and key and
     * <code>null</code> as value to the target collection if there is no property with that group
     * and key in the target collection.
     *
     * @param source
     *            the properties that should be nullified if they are not in the target collection.
     *            Can be null.
     * @param target
     *            the collection of properties to update. Can be null.
     * @return the updated target collection or a new collection if target was null
     */
    public static Collection<StringPropertyTO> nullifyMissingPropertyTOs(
            Iterable<StringPropertyTO> source, Collection<StringPropertyTO> target) {
        if (target == null) {
            target = new ArrayList<StringPropertyTO>();
        }
        if (source != null) {
            Set<Pair<String, String>> existingGroupKeyPairs = extractGroupKeyPairs(target);
            for (StringPropertyTO property : source) {
                if (existingGroupKeyPairs.isEmpty()
                        || !existingGroupKeyPairs.contains(new Pair<String, String>(property
                                .getKeyGroup(), property.getPropertyKey()))) {
                    target.add(new StringPropertyTO(null, property.getKeyGroup(), property
                            .getPropertyKey(), null));
                }
            }
        }
        return target;
    }

    /**
     * Remove the identified property from the provided collection.
     *
     * @param properties
     *            the properties to search
     * @param group
     *            the group key of the property
     * @param key
     *            the key of the property
     * @return the removed property or null if it was not contained
     * @since 3.5
     */
    public static StringPropertyTO removePropertyTO(Iterable<StringPropertyTO> properties,
            String group, String key) {
        if (properties == null || group == null || key == null) {
            return null;
        }
        Iterator<StringPropertyTO> propsIter = properties.iterator();
        while (propsIter.hasNext()) {
            StringPropertyTO property = propsIter.next();
            if (group.equals(property.getKeyGroup()) && key.equals(property.getPropertyKey())) {
                propsIter.remove();
                return property;
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
