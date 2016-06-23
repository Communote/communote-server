package com.communote.server.external.hibernate;

import java.io.Serializable;

/**
 * Key which encodes the client ID so it can be passed to the ClientDelegateCache
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientDelegateCacheSerializableKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Serializable wrappedKey;
    private final String clientId;

    /**
     * Creates a cache key for the ClientDelagateCache
     * 
     * @param clientId
     *            the clientId
     * @param key
     *            the key to be wrapped
     */
    public ClientDelegateCacheSerializableKey(String clientId, Serializable key) {
        this.clientId = clientId;
        this.wrappedKey = key;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ClientDelegateCacheSerializableKey)) {
            return false;
        }
        ClientDelegateCacheSerializableKey other = (ClientDelegateCacheSerializableKey) obj;
        if (!clientId.equals(other.clientId)) {
            return false;
        }
        if (wrappedKey == null) {
            if (other.wrappedKey != null) {
                return false;
            }
        } else if (!wrappedKey.equals(other.wrappedKey)) {
            return false;
        }
        return true;
    }

    /**
     * @return the client ID
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * @return the wrapped key
     */
    public Serializable getWrappedKey() {
        return this.wrappedKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + clientId.hashCode();
        result = prime * result + ((wrappedKey == null) ? 0 : wrappedKey.hashCode());
        return result;
    }
}
