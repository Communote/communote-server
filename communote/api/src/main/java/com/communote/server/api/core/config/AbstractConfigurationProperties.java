package com.communote.server.api.core.config;

import java.util.MissingResourceException;

import org.apache.commons.lang.BooleanUtils;

/**
 * Abstract object that holds configuration properties.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            the types of properties
 */
public abstract class AbstractConfigurationProperties<T extends ConfigurationPropertyConstant> {

    /**
     * Get the property, throw a runtime exception if the key does not exist.
     * 
     * @param key
     *            The key to get
     * @return the property
     */
    public String getAssertProperty(T key) {
        String value = getProperty(key);
        if (value != null) {
            return value;
        }
        throw new MissingResourceException("Key '" + key.getKeyString() + "' is not set!",
                ConfigurationPropertyConstant.class.getName(), key.getKeyString());
    }

    /**
     * Returns the property value for the key as String.
     * 
     * @param key
     *            the key
     * @return the property value or null if there is no property for the key
     */
    public abstract String getProperty(T key);

    /**
     * Returns the property for the key as boolean. 'true', 'on' or 'yes' (case insensitive) will
     * return true. 'false', 'off' or 'no' (case insensitive) will return false. If none of these
     * values is set the fallback will be returned.
     * 
     * @param key
     *            the key
     * @param fallback
     *            the fallback to return if there is no property for the key or the property cannot
     *            be converted to boolean.
     * @return the property value or the fallback if there is no property for the key or it cannot
     *         be converted into a boolean
     */
    public boolean getProperty(T key, boolean fallback) {
        Boolean result = BooleanUtils.toBooleanObject(getProperty(key));
        if (result == null) {
            result = fallback;
        }
        return result;
    }

    /**
     * Returns the property for the key as integer.
     * 
     * @param key
     *            the key
     * @param fallback
     *            the fallback to return if there is no property for the key
     * @return the property value or the fallback if there is no property for the key or it cannot
     *         be converted into an integer
     */
    public int getProperty(T key, int fallback) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Returns the property for the key as long.
     * 
     * @param key
     *            the key
     * @param fallback
     *            the fallback to return if there is no property for the key
     * @return the property value or the fallback if there is no property for the key or it cannot
     *         be converted into a long
     */
    public long getProperty(T key, long fallback) {
        try {
            return Long.parseLong(getProperty(key));
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Returns the property for the key as string.
     * 
     * @param key
     *            the key
     * @param fallback
     *            the fallback to return if there is no property for the key
     * @return the property value or the fallback if there is no property for the key
     */
    public String getProperty(T key, String fallback) {
        String prop = getProperty(key);
        if (prop == null) {
            return fallback;
        }
        return prop;
    }
}
