package com.communote.common.util;

import java.util.Map;

/**
 * Utils for handling maps.
 * 
 * @author Communote team - <a href="http://communote.github.io/">http://communote.github.io/</a>
 *
 */
public class MapUtils {

    private MapUtils() {
        // no construction
    }
    
    /**
     * Add a key value pair to the map iff key and value are not null.
     * 
     * @param map the map to add to, must not be null
     * @param key the key to add
     * @param value the value to add
     */
    public static <K, V> void putNonNull(Map<K, V> map, K key, V value) {
        if (key != null && value != null) {
            map.put(key, value);
        }
    }
}
