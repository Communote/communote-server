package com.communote.plugins.mq.message.core.util;

/**
 * Policy for handling different parameters
 * 
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum StoringPolicy {

    /**
     * Replace existing with new items
     */
    SET_NEW,
    /**
     * Ignore any new items and keep existing
     */
    PRESERVE_EXISTING,
    /**
     * Merge existing with new items
     */
    MERGE,
    /**
     * Ignore new items and delete existing
     */
    DELETE
}