package com.communote.server.core.common.caching;

import java.io.Serializable;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * An abstract cache providing basic caching features. The get method follows a self-populating
 * strategy.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cache.class);

    private static final String CACHE_KEY_DELIMITER = ".";
    private static final String CACHE_KEY_APPLICATION_PREFIX = "$app$";
    private static final String CLIENT_CONTENT_TYPE_DELIMITER = " ";

    private final HashMap<String, Long> dirtyCaches = new HashMap<>();
    private final HashMap<String, Long> dirtyClientCaches = new HashMap<>();

    /**
     * Creates the actual cache key used in caching backend to store data.
     *
     * @param <K>
     *            the type of the key
     * @param <T>
     *            the type of the cached element
     * @param key
     *            the key to use
     * @param provider
     *            the provider to load the requested element in case it is not cached
     * @return the actual cache key
     */
    private <K extends CacheKey, T extends Serializable> String createCacheKey(K key,
            CacheElementProvider<K, T> provider) {
        StringBuilder cacheKeySb = new StringBuilder();
        if (key.isUniquePerClient()) {
            cacheKeySb.append(ClientHelper.getCurrentClientId());
        } else {
            cacheKeySb.append(CACHE_KEY_APPLICATION_PREFIX);
        }
        cacheKeySb.append(Cache.CACHE_KEY_DELIMITER);
        cacheKeySb.append(provider.getContentType());
        cacheKeySb.append(Cache.CACHE_KEY_DELIMITER);
        cacheKeySb.append(key.getCacheKeyString());
        return cacheKeySb.toString();
    }

    /**
     * This method checks the access for the given item, before it returns it.
     *
     * @param <K>
     *            the type of the key
     * @param <T>
     *            the type of the cached element
     * @param key
     *            the key to use
     * @param provider
     *            The provider for asserting access and loading the requested element in case it is
     *            not cached.
     * @return the cached element or null if the element provider returned null or threw a
     *         CacheElementProviderException
     * @throws AuthorizationException
     *             Thrown, if the user has no access to the given item.
     */
    public <K extends CacheKey, T extends Serializable> T get(K key,
            AccessRestrictedCacheElementProvider<K, T> provider) throws AuthorizationException {
        provider.assertAccess(key);
        return get(key, (CacheElementProvider<K, T>) provider);
    }

    /**
     * Retrieves an element identified by the provided key from the cache. In case this element is
     * not yet cached it will be cached after loading it from the element provider. If the element
     * provider cannot provide the requested element nothing will be cached.
     *
     * @param <K>
     *            the type of the key
     * @param <T>
     *            the type of the cached element
     * @param key
     *            the key to use
     * @param provider
     *            the provider for loading the requested element in case it is not cached
     * @return the cached element or null if the element provider returned null or threw a
     *         CacheElementProviderException
     */
    @SuppressWarnings("unchecked")
    public <K extends CacheKey, T extends Serializable> T get(K key,
            CacheElementProvider<K, T> provider) {
        String cacheKey = createCacheKey(key, provider);
        CacheElementWrapper<Serializable> cachedElement = null;
        try {
            cachedElement = handleGet(cacheKey);
        } catch (CacheException e) {
            LOGGER.debug("CacheException.", e);
            // ignore, load
        }
        T cachedValue;
        if (cachedElement == null || isInvalid(cachedElement, provider)) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(provider.getClass().getSimpleName() + "#" + key.getCacheKeyString()
                        + " cache miss. ");
            }
            cachedValue = null;
            try {
                cachedValue = provider.load(key);
                handlePut(cacheKey, new CacheElementWrapper<Serializable>(cachedValue),
                        provider.getTimeToLive());
            } catch (CacheException e) {
                LOGGER.debug("CacheException.", e);
                // ignore, try again next time
            } catch (CacheElementProviderException e) {
                // TODO let the provider define a custom exception to throw?
                LOGGER.debug("Loading from provider failed: {}", e.getMessage());
            }

        } else {
            cachedValue = (T) cachedElement.getElement();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(provider.getClass().getSimpleName() + "#" + key.getCacheKeyString()
                        + " cache hit. ");
            }
        }
        return cachedValue;
    }

    /**
     * Retrieves an element identified by the provided key from the cache.
     *
     * @param cacheKey
     *            the key used for storing the element in the cache
     * @return the cached element or null if there is no element cached that has the supplied
     *         cacheKey or the cached element is expired
     * @throws CacheException
     *             when there are problems with the backend
     */
    protected abstract CacheElementWrapper<Serializable> handleGet(String cacheKey)
            throws CacheException;

    /**
     * Invalidates the data stored under the supplied key.
     *
     * @param cacheKey
     *            the key used for caching
     * @throws CacheException
     *             when the invalidation failed
     */
    protected abstract void handleInvalidate(String cacheKey) throws CacheException;

    /**
     * Stores an element under the key cacheKey in the cache.
     *
     * @param cacheKey
     *            the key used for caching and retrieving
     * @param cacheElement
     *            the element to be cached; never null
     * @param timeToLive
     *            the time in seconds until the item will expire. A negative value indicates eternal
     *            caching.
     * @throws CacheException
     *             when caching failed
     */
    protected abstract void handlePut(String cacheKey,
            CacheElementWrapper<Serializable> cacheElement, int timeToLive) throws CacheException;

    /**
     * Invalidates a cached element that has the key cacheKey.
     *
     * @param <K>
     *            the type of the key
     * @param <T>
     *            the type of the cached element
     * @param key
     *            the key to use
     * @param provider
     *            the associated provider
     * @throws CacheException
     *             when the invalidation failed
     */
    public <K extends CacheKey, T extends Serializable> void invalidate(K key,
            CacheElementProvider<K, T> provider) throws CacheException {
        String cacheKey = createCacheKey(key, provider);
        handleInvalidate(cacheKey);
    }

    /**
     * Invalidate all cached items that were retrieved from the given provider.
     *
     * Note: this will invalidate the cached items of all clients
     *
     * @param provider
     *            the provider from which the cached items to be invalidated were loaded
     */
    public void invalidateAll(CacheElementProvider<?, ?> provider) {
        // lazy invalidation strategy: we do not know what items are in the cache, thus, invalidate
        // all items that were cached before current time
        // TODO not cluster safe. To make it cluster save send a distributable event
        dirtyCaches.put(provider.getContentType(), System.currentTimeMillis());
    }

    /**
     * Invalidate all cached items that were retrieved from the given provider.
     *
     *
     * @param provider
     *            the provider from which the cached items to be invalidated were loaded
     */
    public void invalidateAllOfCurrentClient(CacheElementProvider<?, ?> provider) {
        // TODO not cluster safe. To make it cluster save send a distributable event
        if (CommunoteRuntime.getInstance().getApplicationInformation().isStandalone()) {
            this.invalidateAll(provider);
        } else {
            dirtyClientCaches.put(ClientHelper.getCurrentClientId() + CLIENT_CONTENT_TYPE_DELIMITER
                    + provider.getContentType(), System.currentTimeMillis());
        }
    }

    /**
     * Test whether a cached element was invalidated by a call to invalidateAll or
     * invalidateAllOfCurrentClient.
     *
     * @param cachedElement
     *            the element to test
     * @param provider
     *            the provider of the element
     * @return true if the cached element is invalid
     */
    private boolean isInvalid(CacheElementWrapper<?> cachedElement,
            CacheElementProvider<?, ?> provider) {
        Long invalidationTimestamp = dirtyCaches.get(provider.getContentType());
        if (dirtyClientCaches.size() > 0) {
            Long clientInvalidationTimestamp = dirtyClientCaches.get(ClientHelper
                    .getCurrentClientId()
                    + CLIENT_CONTENT_TYPE_DELIMITER
                    + provider.getContentType());
            if (clientInvalidationTimestamp != null) {
                if (invalidationTimestamp == null
                        || invalidationTimestamp < clientInvalidationTimestamp) {
                    invalidationTimestamp = clientInvalidationTimestamp;
                }
            }
        }
        if (invalidationTimestamp != null) {
            return cachedElement.getCreationTimestamp() < invalidationTimestamp;
        }
        return false;
    }

    /**
     * Stores some data in the cache under the provided key.
     *
     * @param <K>
     *            the type of the key
     * @param <T>
     *            the type of the data to be cached
     * @param key
     *            the key to use
     * @param provider
     *            the associated provider
     * @param data
     *            the data to be cached; can be null
     * @throws CacheException
     *             when caching failed
     */
    public <K extends CacheKey, T extends Serializable> void put(K key,
            CacheElementProvider<K, T> provider, T data) throws CacheException {
        String cacheKey = createCacheKey(key, provider);
        // create wrapper with current time
        handlePut(cacheKey, new CacheElementWrapper<Serializable>(data), provider.getTimeToLive());
    }
}
