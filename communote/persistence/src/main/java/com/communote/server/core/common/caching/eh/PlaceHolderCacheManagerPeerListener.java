package com.communote.server.core.common.caching.eh;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Status;
import net.sf.ehcache.distribution.CacheManagerPeerListener;

/**
 * A CacheManagerPeerListener which is a place holder for a peer listener of a specific replication
 * scheme. This allows adding a peer listener after the cache manager has been initialized.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class PlaceHolderCacheManagerPeerListener implements CacheManagerPeerListener {

    private final String scheme;
    private CacheManagerPeerListener peerListener;
    private final String uniqueIdentifier;

    /**
     * Creates a new place holder for a real peer listener matching the replication scheme.
     * 
     * @param scheme
     *            the replication scheme
     */
    public PlaceHolderCacheManagerPeerListener(String scheme) {
        this.scheme = scheme;
        this.uniqueIdentifier = "peerListenerPlaceHolderFor" + scheme;
    }

    /**
     * Attaches and initializes a peer listener to be wrapped by the place holder. The peer listener
     * will than be available to the cache manager.
     * 
     * @param peerListener
     *            the peer listener to add
     * @throws CacheException
     *             in case there is already a peer listener attached or the scheme of the peer
     *             listener does not match
     */
    public synchronized void attachCacheManagerPeerListener(CacheManagerPeerListener peerListener)
            throws CacheException {
        if (this.peerListener != null) {
            throw new CacheException("This place holder already wraps a peer provider");
        }
        if (!peerListener.getScheme().equals(scheme)) {
            throw new CacheException(
                    "The scheme of the peer provider does not match the scheme of the place holder");
        }
        this.peerListener = peerListener;
        this.peerListener.init();
    }

    /**
     * {@inheritDoc}
     */
    public void attemptResolutionOfUniqueResourceConflict() throws IllegalStateException,
            CacheException {
        if (this.peerListener != null) {
            this.peerListener.attemptResolutionOfUniqueResourceConflict();
        } else {
            throw new IllegalStateException("The unique resource conflict cannot be resolved "
                    + "as long as no peer listener is attached");
        }

    }

    /**
     * {@inheritDoc}
     */
    public void dispose() throws CacheException {
        if (this.peerListener != null) {
            this.peerListener.dispose();
        }

    }

    /**
     * {@inheritDoc}
     */
    public List getBoundCachePeers() {
        if (this.peerListener != null) {
            return this.peerListener.getBoundCachePeers();
        }
        return new ArrayList(0);

    }

    /**
     * {@inheritDoc}
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * {@inheritDoc}
     */
    public Status getStatus() {
        if (this.peerListener != null) {
            return this.peerListener.getStatus();
        }
        return Status.STATUS_ALIVE;
    }

    /**
     * {@inheritDoc}
     */
    public String getUniqueResourceIdentifier() {
        if (this.peerListener != null) {
            return this.peerListener.getUniqueResourceIdentifier();
        }
        return this.uniqueIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    public void init() {
        if (this.peerListener != null) {
            this.peerListener.init();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void notifyCacheAdded(String cacheName) {
        if (this.peerListener != null) {
            this.peerListener.notifyCacheAdded(cacheName);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void notifyCacheRemoved(String cacheName) {
        if (this.peerListener != null) {
            this.peerListener.notifyCacheRemoved(cacheName);
        }
    }

}
