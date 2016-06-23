package com.communote.server.core.property;

import com.communote.server.core.common.caching.CacheKey;

/**
 * Key to cache the UserNoteProperty instances added to a note. The properties are cached per
 * property group and key combination.
 * 
 * @see UserNotePropertyCacheElementProvider
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserNotePropertyCacheKey implements CacheKey {

    private final String propertyKeyGroup;
    private final String propertyKey;
    private final Long noteId;
    private final String cacheKey;

    /**
     * Create a new cache key
     * 
     * @param noteId
     *            the ID of the note for which the properties are cached
     * @param propertyKeyGroup
     *            the key group of the property for which the property values and users are cached
     * @param propertyKey
     *            the key of the property for which the property values and users are cached
     */
    public UserNotePropertyCacheKey(Long noteId, String propertyKeyGroup, String propertyKey) {
        this.noteId = noteId;
        this.propertyKeyGroup = propertyKeyGroup;
        this.propertyKey = propertyKey;
        this.cacheKey = noteId.toString() + " " + propertyKeyGroup + " " + propertyKey;
    }

    @Override
    public String getCacheKeyString() {
        return cacheKey;
    }

    /**
     * @return the ID of the note for which the properties are cached
     */
    public Long getNoteId() {
        return noteId;
    }

    /**
     * @return the key of the property for which the property values and users are cached
     */
    public String getPropertyKey() {
        return propertyKey;
    }

    /**
     * @return the key group of the property for which the property values and users are cached
     */
    public String getPropertyKeyGroup() {
        return propertyKeyGroup;
    }

    @Override
    public boolean isUniquePerClient() {
        return true;
    }

}
