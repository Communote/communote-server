package com.communote.server.core.common.caching.eh;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;

/**
 * A CacheManagerPeerProvider which is a placeholder for a peer provider of a specific replication
 * scheme. This allows adding a peer provider after the cache manager has been initialized.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class PlaceHolderCacheManagerPeerProvider implements CacheManagerPeerProvider {

    private final String scheme;
    private CacheManagerPeerProvider peerProvider;

    /**
     * Creates a new place holder for a real peer provider matching the replication scheme.
     * 
     * @param scheme
     *            the replication scheme
     */
    public PlaceHolderCacheManagerPeerProvider(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Attaches and initializes a peer provider to be wrapped by the place holder. The peer provider
     * will than be available to the cache manager.
     * 
     * @param peerProvider
     *            the peer provider to add
     * @throws CacheException
     *             in case there is already a peer provider attached or the scheme of the peer
     *             provider does not match
     */
    public synchronized void attachCacheManagerPeerProvider(CacheManagerPeerProvider peerProvider)
            throws CacheException {
        if (this.peerProvider != null) {
            throw new CacheException("This placeholder already wraps a peer provider");
        }
        if (!peerProvider.getScheme().equals(scheme)) {
            throw new CacheException(
                    "The scheme of the peer provider does not match the scheme of the place holder");
        }
        this.peerProvider = peerProvider;
        this.peerProvider.init();
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() throws CacheException {
        if (this.peerProvider != null) {
            this.peerProvider.dispose();
        }

    }

    /**
     * {@inheritDoc}
     */
    public String getScheme() {
        return this.scheme;
    }

    /**
     * {@inheritDoc}
     */
    public long getTimeForClusterToForm() {
        if (this.peerProvider != null) {
            return this.peerProvider.getTimeForClusterToForm();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void init() {
        if (this.peerProvider != null) {
            this.peerProvider.init();
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List listRemoteCachePeers(Ehcache cache) throws CacheException {
        if (this.peerProvider != null) {
            return this.peerProvider.listRemoteCachePeers(cache);
        }
        return new ArrayList();
    }

    /**
     * {@inheritDoc}
     */
    public void registerPeer(String rmiUrl) {
        if (this.peerProvider != null) {
            this.peerProvider.registerPeer(rmiUrl);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterPeer(String rmiUrl) {
        if (this.peerProvider != null) {
            this.peerProvider.registerPeer(rmiUrl);
        }
    }

}
