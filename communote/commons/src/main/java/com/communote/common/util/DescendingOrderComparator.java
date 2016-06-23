package com.communote.common.util;

import java.util.Comparator;

/**
 * Comparator for sorting orderable objects in descending order. Objects with a higher order value
 * will be first in the sorted result.
 * 
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DescendingOrderComparator implements Comparator<Orderable> {

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(Orderable orderable1, Orderable orderable2) {
        int retVal = 0;
        if (orderable1.getOrder() > orderable2.getOrder()) {
            retVal = -1;
        } else if (orderable1.getOrder() < orderable2.getOrder()) {
            retVal = 1;
        }
        return retVal;
    }

}
