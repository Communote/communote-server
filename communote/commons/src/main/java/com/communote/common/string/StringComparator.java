package com.communote.common.string;

import java.util.Comparator;

/**
 * Comparator for comparing two objects by calling the toString method. Takes care of null values.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StringComparator implements Comparator<Object> {

    /**
     * {@inheritDoc}
     */
    public int compare(Object o1, Object o2) {
        // both are equal or both are null
        if (o1 == o2) {
            return 0;
        }
        // o1 is null, o2 is not
        if (o1 == null) {
            return 1;
        }
        // o2 is null, o1 is not
        if (o2 == null) {
            return -1;
        }
        // finally compare the strings
        return o1.toString().compareTo(o2.toString());
    }

}
