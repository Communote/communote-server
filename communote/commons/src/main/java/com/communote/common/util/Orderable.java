package com.communote.common.util;

/**
 * Interface to be implemented by objects that should be orderable. This interface does not define
 * an interpretation of the actual order.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface Orderable {

    /**
     * @return the value for ordering this object
     */
    int getOrder();
}
