package com.communote.server.web.commons.tags;

import java.util.Collection;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SupportingTaglibFunctions {

    /**
     * Checks if a collection contains an element
     * 
     * @param coll
     *            the collection, can be null
     * @param element
     *            the element, can be null
     * @return coll.contains(element)
     */
    public static boolean contains(Collection<?> coll, Object element) {
        return coll == null ? false : coll.contains(element);
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private SupportingTaglibFunctions() {
        // Do nothing
    }

}
