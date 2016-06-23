package com.communote.server.core.property;

import java.util.Date;
import java.util.Set;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.common.caching.Cache;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.vo.IdDateTO;
import com.communote.server.model.property.BinaryProperty;
import com.communote.server.model.property.Propertyable;
import com.communote.server.model.property.StringProperty;
import com.communote.server.persistence.property.BinaryPropertyDao;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class BinaryPropertyAccessor extends PropertyAccessor<Propertyable, BinaryProperty, byte[]> {

    /**
     * Cache provider that loads the ID and last modification date of a binary property.
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     */
    private static class BinaryPropertyIdProvider implements
    CacheElementProvider<PropertyCacheKey, IdDateTO> {

        @Override
        public String getContentType() {
            return "property";
        }

        @Override
        public int getTimeToLive() {
            return 3600;
        }

        @Override
        public IdDateTO load(PropertyCacheKey key) {
            return ServiceLocator.findService(BinaryPropertyDao.class)
                    .findIdByKey(key.getKeyGroup(), key.getKey());
        }
    }

    /**
     * Dummy class since binary properties are not bound to any context
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     *
     */
    public static final class DummyPropertyable implements Propertyable {
        /**
         * This throws an {@link UnsupportedOperationException}.
         *
         * {@inheritDoc}
         */
        @Override
        public Set<? extends StringProperty> getProperties() {
            throw new UnsupportedOperationException("Not supported!");
        }

    }

    /**
     * DUMMY Object since the binary properties are not bound to a object instance
     */
    public static final DummyPropertyable DUMMY_OBJECT = new DummyPropertyable();

    private final BinaryPropertyIdProvider propertyIdProvider;
    private CacheManager cacheManager;

    /**
     *
     * @param eventDispatcher
     *            the event dispatcher for dispatching event on property changes
     */
    public BinaryPropertyAccessor(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
        propertyIdProvider = new BinaryPropertyIdProvider();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Does nothing, as everyone is allowed to read binary properties.
     * </p>
     */
    @Override
    protected void assertReadAccess(Propertyable propertyable) {
        // Do nothing.
    }

    @Override
    protected boolean assertValidForCreateAndUpdate(String keyGroup, String key) {
        if (keyGroup == null) {
            throw new IllegalArgumentException("keyGroup cannot be null!");
        }
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null!");
        }
        return true;
    }

    /**
     * Does nothing.
     *
     * @param propertyable
     *            Not used.
     */
    @Override
    protected void assertWriteAccess(Propertyable propertyable) {
        // Do nothing.
    }

    /**
     *
     * @return the binary property dao
     */
    private BinaryPropertyDao getBinaryPropertyDao() {
        return ServiceLocator.findService(BinaryPropertyDao.class);
    }

    /**
     * @return the lazily initialized cache manager
     */
    private CacheManager getCacheManager() {
        if (cacheManager == null) {
            cacheManager = ServiceLocator.findService(CacheManager.class);
        }
        return cacheManager;
    }

    /**
     * Get last modification date of a binary property.
     *
     * @param keyGroup
     *            group of the property key
     * @param key
     *            the key of the property
     * @return the last modification date or null if the property does not exist
     */
    public Date getLastModificationDate(String keyGroup, String key) {
        IdDateTO propertyData = getPropertyData(keyGroup, key);
        if (propertyData != null) {
            return propertyData.getDate();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long getObjectId(Propertyable object) {
        return 0L;
    }

    private IdDateTO getPropertyData(String keyGroup, String key) {
        PropertyCacheKey cacheKey = new PropertyCacheKey(PropertyType.BinaryProperty, keyGroup, key);
        Cache cache = getCacheManager().getCache();
        IdDateTO propertyData;
        if (cache != null) {
            propertyData = cache.get(cacheKey, propertyIdProvider);
        } else {
            propertyData = propertyIdProvider.load(cacheKey);
        }
        return propertyData;
    }

    /**
     * Get the ID of a binary property
     *
     * @param keyGroup
     *            The group of the property key
     * @param key
     *            The key of the property
     * @param key
     * @return the id or null if the property does not exist
     */
    private Long getPropertyId(String keyGroup, String key) {
        IdDateTO propertyData = getPropertyData(keyGroup, key);
        if (propertyData != null) {
            return propertyData.getId();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyType getPropertyType() {
        return PropertyType.BinaryProperty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BinaryProperty handleCreateNewProperty(Propertyable object) {
        return BinaryProperty.Factory.newInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BinaryProperty handleGetObjectPropertyUnfiltered(Propertyable object,
            String keyGroup, String key) {
        Long id = getPropertyId(keyGroup, key);
        if (id != null) {
            return getBinaryPropertyDao().load(id);
        }
        return null;
    }

    @Override
    protected void handleRemoveObjectProperty(Propertyable object, String keyGroup, String key)
            throws AuthorizationException {
        // overridden to get the cache invalidated
        internalRemoveProperty(keyGroup, key);
    }

    /**
     * @param object
     *            The object.
     * @param keyGroup
     *            The keygroup.
     * @param key
     *            The key.
     * @return True, when the property exists.
     */
    public boolean hasProperty(Propertyable object, String keyGroup, String key) {
        return getPropertyId(keyGroup, key) != null;
    }

    /**
     * Removes the given property.
     *
     * @param keyGroup
     *            The group of the property key
     * @param key
     *            The key of the property
     *
     */
    private void internalRemoveProperty(String keyGroup, String key) {
        Long id = getPropertyId(keyGroup, key);
        if (id != null) {
            getBinaryPropertyDao().remove(id);
            this.invalidateCache(keyGroup, key);
        }
    }

    /**
     * Invalidate a cached entry for the property
     *
     * @param keyGroup
     *            the group of the property key
     * @param key
     *            the key of the property
     */
    private void invalidateCache(String keyGroup, String key) {
        PropertyCacheKey cacheKey = new PropertyCacheKey(PropertyType.BinaryProperty, keyGroup, key);
        Cache cache = getCacheManager().getCache();
        if (cache != null) {
            cache.invalidate(cacheKey, propertyIdProvider);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Propertyable load(Long id) {
        return DUMMY_OBJECT;
    }

    /**
     * Removes the given property.
     *
     * @param keyGroup
     *            The group of the property key
     * @param key
     *            The key of the property
     *
     */
    public void removeProperty(String keyGroup, String key) {
        internalRemoveProperty(keyGroup, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setPropertyValue(BinaryProperty property, byte[] value) {
        property.setPropertyValue(value);
        this.invalidateCache(property.getKeyGroup(), property.getPropertyKey());
    }
}
