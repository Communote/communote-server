package com.communote.server.core.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.communote.server.persistence.user.client.ClientHelper;

/**
 * The client value stores a value separately for each client using
 * {@link ClientHelper#getCurrentClientId()}
 *
 * The {@link #hashCode()}, {@link #equals(Object)} and {@link #toString()} methods also use the
 * current client and delegate the logic to a check against the value of the client.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 */
public class ClientValue<T> {

    private final Map<String, T> valuesPerClient = new ConcurrentHashMap<>();

    @Override
    public boolean equals(Object obj) {
        T value = getValue();

        if (value != null) {
            return value.equals(obj);
        }
        if (obj != null) {
            return obj.equals(value);
        }

        return value == obj;
    }

    public T getValue() {
        return valuesPerClient.get(ClientHelper.getCurrentClientId());
    }

    @Override
    public int hashCode() {
        T value = getValue();
        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public void setValue(T value) {
        if (value == null) {
            valuesPerClient.remove(ClientHelper.getCurrentClientId());
        } else {
            valuesPerClient.put(ClientHelper.getCurrentClientId(), value);
        }
    }

    @Override
    public String toString() {
        String clientId = ClientHelper.getCurrentClientId();

        if (clientId == null) {
            return "(no client)";
        }
        return clientId + " " + getValue();
    }
}
