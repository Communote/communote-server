package com.communote.server.core.property;

import com.communote.server.api.core.property.PropertyType;
import com.communote.server.core.common.caching.CacheKey;

/**
 * Cache key to identify a property by group and key
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PropertyCacheKey implements CacheKey {

    private final PropertyType type;

    private final String keyGroup;
    private final String key;

    /**
     * Cache key to identify a property by group and key
     * 
     * @param type
     *            the type of the property
     * @param keyGroup
     *            the group of the key of the property
     * @param key
     *            the key of the property
     */
    public PropertyCacheKey(PropertyType type, String keyGroup, String key) {
        this.keyGroup = keyGroup;
        this.key = key;
        this.type = type;
    }

    @Override
    public String getCacheKeyString() {
        return type + "_" + keyGroup + "_" + key;
    }

    /**
     * @return the key of the property
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the group of the key of the property
     */
    public String getKeyGroup() {
        return keyGroup;
    }

    @Override
    public boolean isUniquePerClient() {
        return true;
    }

}
