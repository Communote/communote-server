package com.communote.plugins.api.rest.v30.resource;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;

/**
 * Wraps a {@link MultivaluedMap} so that by default the first entry will be returned at get.
 * However the wrapper can be configured to join parameters by "," if the parameters name is defined
 * in {@link #commaSeparatedParameters} (as passed to the constructor).
 * 
 * For example if the values "f_userIds" is set in the {@link #commaSeparatedParameters} list then
 * get() will join all values of the multivalued map. If the multivalued map contains two value
 * "1,2" and "45" then the result will be "1,2,45"
 * 
 * @param <T>
 *            type of the value
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RestApiMultivaluedMapWrapper<T extends Object> implements Map<String, T> {

    private final MultivaluedMap<String, String> multivaluedMap;

    private final Collection<String> commaSeparatedParameters;

    /**
     * @param multivaluedMap
     *            The map to wrap.
     */
    public RestApiMultivaluedMapWrapper(MultivaluedMap<String, String> multivaluedMap) {
        this(multivaluedMap, Collections.EMPTY_SET);
    }

    /**
     * @param multivaluedMap
     *            The map to wrap.
     * @param commaSeparatedParameters
     *            {@link #commaSeparatedParameters} (as passed to the constructor) then all values
     *            are joined by ","
     */
    public RestApiMultivaluedMapWrapper(MultivaluedMap<String, String> multivaluedMap,
            Collection<String> commaSeparatedParameters) {
        this.commaSeparatedParameters = commaSeparatedParameters;
        this.multivaluedMap = multivaluedMap;
    }

    /**
     * Calls multivaluedMap.clear()
     */
    @Override
    public void clear() {
        multivaluedMap.clear();
    }

    /**
     * {@inheritDoc}
     * 
     * @return multivaluedMap.containsKey(key)
     */
    @Override
    public boolean containsKey(Object key) {
        return multivaluedMap.containsKey(key);
    }

    /**
     * {@inheritDoc}
     * 
     * @return multivaluedMap.containsValue(value)
     */
    @Override
    public boolean containsValue(Object value) {
        return multivaluedMap.containsValue(value);
    }

    /**
     * Not supported. Throws an {@link UnsupportedOperationException}.
     * 
     * @return Nothing.
     */
    @Override
    public Set<java.util.Map.Entry<String, T>> entrySet() {
        Set<Entry<String, T>> entrySet = new HashSet<>();
        for (String key : keySet()) {
            entrySet.add(new AbstractMap.SimpleEntry<String, T>(key, get(key)));
        }

        return entrySet;
    }

    /**
     * If the wrapped multivalued map contains several values and the key is defined in
     * {@link #commaSeparatedParameters} (as passed to the constructor) then all values are joined
     * by ","
     * 
     * @param key
     *            The key.
     * @return multivaluedMap#getFirst(key)
     */
    @Override
    public T get(Object key) {
        if (commaSeparatedParameters.contains(key) && multivaluedMap.containsKey(key)) {
            List<String> result = multivaluedMap.get(key);
            return (T) StringUtils.join(result, ",");
        }
        return (T) multivaluedMap.getFirst((String) key);
    }

    /**
     * @return multivaluedMap.isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return multivaluedMap.isEmpty();
    }

    /**
     * @return multivaluedMap.keySet()
     */
    @Override
    public Set<String> keySet() {
        return multivaluedMap.keySet();
    }

    /**
     * Calls multivaluedMap.putSingle(key, value)
     * 
     * @param key
     *            The key.
     * @param value
     *            The value
     * @return The first value, if there is one.
     */
    public String put(String key, String value) {
        String first = multivaluedMap.getFirst(key);
        multivaluedMap.putSingle(key, value);
        return first;
    }

    /**
     * Calls multivaluedMap.putSingle(key, value)
     * 
     * type of the value
     * 
     * @param key
     *            The key.
     * @param value
     *            The value
     * @return value of MultivaluedMap
     */
    @Override
    public T put(String key, T value) {
        T first = (T) multivaluedMap.getFirst(key);
        multivaluedMap.putSingle(key, value.toString());
        return first;
    }

    /**
     * Calls multivaluedMap.putSingle(key, value)
     * 
     * type of the value
     * 
     * @param m
     *            map of the same object
     */
    @Override
    public void putAll(Map<? extends String, ? extends T> m) {
        throw new UnsupportedOperationException();

    }

    /**
     * {@inheritDoc}
     * 
     * @return Removes all elements, but returns the first one.
     */
    @Override
    public T remove(Object key) {
        String first = multivaluedMap.getFirst((String) key);
        multivaluedMap.remove(key);
        return (T) first;
    }

    /**
     * @return multivaluedMap.size()
     */
    @Override
    public int size() {
        return multivaluedMap.size();
    }

    /**
     * {@inheritDoc}
     * 
     * @return Returns a list of the first values.
     */
    @Override
    public Collection<T> values() {
        Collection<String> result = new ArrayList<String>();
        for (List<String> value : multivaluedMap.values()) {
            if (value != null && value.size() > 0) {
                result.add(value.get(0));
            }
        }
        return null;
    }
}