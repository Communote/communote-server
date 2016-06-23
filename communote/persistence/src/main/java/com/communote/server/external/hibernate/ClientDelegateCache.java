package com.communote.server.external.hibernate;

import java.io.Serializable;
import java.util.Map;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;

import com.communote.server.persistence.user.client.ClientHelper;


/**
 * Cache wrapper which will delegate cache requests to the correct cache for the current client by
 * manipulating the cache key.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ClientDelegateCache implements Cache {

    private final Cache cacheBackend;

    /**
     * Creates a new instance from the cache to wrapped
     * 
     * @param cacheBackend
     *            the cache to be wrapped
     */
    public ClientDelegateCache(Cache cacheBackend) {
        this.cacheBackend = cacheBackend;
    }

    /**
     * {@inheritDoc}
     */
    public void clear() throws CacheException {
        cacheBackend.clear();
    }

    /**
     * Creates a cache key that is aware of the current client ID
     * 
     * @param key
     *            the key process
     * @return the client aware cache key
     * @throws CacheException
     *             in case the key is not serializable
     */
    private ClientDelegateCacheSerializableKey createCacheKey(Object key) throws CacheException {
        if (!(key instanceof Serializable)) {
            throw new CacheException("The key must be serializable");
        }
        Serializable serializableKey = (Serializable) key;
        String clientId = ClientHelper.getCurrentClientId();
        return new ClientDelegateCacheSerializableKey(clientId, serializableKey);
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() throws CacheException {
        cacheBackend.destroy();
    }

    /**
     * {@inheritDoc}
     */
    public Object get(Object key) throws CacheException {
        return cacheBackend.get(createCacheKey(key));
    }

    /**
     * {@inheritDoc}
     */
    public long getElementCountInMemory() {
        return cacheBackend.getElementCountInMemory();
    }

    /**
     * {@inheritDoc}
     */
    public long getElementCountOnDisk() {
        return cacheBackend.getElementCountOnDisk();
    }

    /**
     * {@inheritDoc}
     */
    public String getRegionName() {
        return cacheBackend.getRegionName();
    }

    /**
     * {@inheritDoc}
     */
    public long getSizeInMemory() {
        return cacheBackend.getSizeInMemory();
    }

    /**
     * {@inheritDoc}
     */
    public int getTimeout() {
        return cacheBackend.getTimeout();
    }

    /**
     * {@inheritDoc}
     */
    public void lock(Object key) throws CacheException {
        cacheBackend.lock(createCacheKey(key));
    }

    /**
     * {@inheritDoc}
     */
    public long nextTimestamp() {
        return cacheBackend.nextTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    public void put(Object key, Object value) throws CacheException {
        cacheBackend.put(createCacheKey(key), value);
    }

    /**
     * {@inheritDoc}
     */
    public Object read(Object key) throws CacheException {
        return cacheBackend.read(createCacheKey(key));
    }

    /**
     * {@inheritDoc}
     */
    public void remove(Object key) throws CacheException {
        cacheBackend.remove(createCacheKey(key));
    }

    /**
     * {@inheritDoc}
     */
    public Map toMap() {
        return cacheBackend.toMap();
    }

    /**
     * {@inheritDoc}
     */
    public void unlock(Object key) throws CacheException {
        cacheBackend.unlock(createCacheKey(key));
    }

    /**
     * {@inheritDoc}
     */
    public void update(Object key, Object value) throws CacheException {
        cacheBackend.update(createCacheKey(key), value);
    }

}
