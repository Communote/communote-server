package com.communote.server.core.plugin;

import com.communote.server.core.common.caching.CacheKey;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            The type of this property.
 */
public class PluginPropertyCacheKey<T> implements CacheKey {

    private final boolean isApplicationProperty;

    private final Class<T> type;

    private final String symbolicName;

    private final String propertyKey;

    private final boolean loadAllProperties;

    /**
     * Constructor.
     * 
     * @param symbolicName
     *            The symbolic name.
     * @param applicationProperty
     *            True, if all application properties should be loaded.
     * @param loadAllProperties
     *            If set, all properties will be loaded and not just a single one.
     */
    public PluginPropertyCacheKey(String symbolicName, boolean applicationProperty,
            boolean loadAllProperties) {
        this(symbolicName, null, applicationProperty, null, loadAllProperties);
    }

    /**
     * Constructor.
     * 
     * @param symbolicName
     *            The symbolic name.
     * @param propertyKey
     *            The properties key.
     * @param isApplicationProperty
     *            True, if this is an application property.
     */
    public PluginPropertyCacheKey(String symbolicName, String propertyKey,
            boolean isApplicationProperty) {
        this(symbolicName, propertyKey, isApplicationProperty, null, false);
    }

    /**
     * Constructor.
     * 
     * @param symbolicName
     *            The symbolic name.
     * @param propertyKey
     *            The properties key.
     * @param isApplicationProperty
     *            True, if this is an application property.
     * @param type
     *            Class holding the propertys type.
     */
    public PluginPropertyCacheKey(String symbolicName, String propertyKey,
            boolean isApplicationProperty, Class<T> type) {
        this(symbolicName, propertyKey, isApplicationProperty, type, false);
    }

    /**
     * Constructor.
     * 
     * @param symbolicName
     *            The symbolic name.
     * @param propertyKey
     *            The properties key.
     * @param isApplicationProperty
     *            True, if this is an application property.
     * @param type
     *            Class holding the propertys type.
     * @param loadAllProperties
     *            If set, all properties will be loaded and npt just a single one. The type will be
     *            ignored if this is set.
     * */
    private PluginPropertyCacheKey(String symbolicName, String propertyKey,
            boolean isApplicationProperty, Class<T> type, boolean loadAllProperties) {
        this.loadAllProperties = loadAllProperties;
        this.symbolicName = symbolicName;
        this.propertyKey = propertyKey;
        this.isApplicationProperty = isApplicationProperty;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCacheKeyString() {
        return getSymbolicName() + "|" + getPropertyKey() + "|"
                + loadAllProperties
                + isApplicationProperty;
    }

    /**
     * @return the propertyKey
     */
    public String getPropertyKey() {
        return propertyKey;
    }

    /**
     * @return the symbolicName
     */
    public String getSymbolicName() {
        return symbolicName;
    }

    /**
     * @return the type
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * @return the isApplicationProperty
     */
    public boolean isApplicationProperty() {
        return isApplicationProperty;
    }

    /**
     * @return the loadAllProperties
     */
    public boolean isLoadAllProperties() {
        return loadAllProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUniquePerClient() {
        return !isApplicationProperty;
    }
}
