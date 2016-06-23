package com.communote.common.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for array operations
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ArrayHelper {

    /**
     * Remove duplicate entries in the array. The order will not be maintained!
     * 
     * @param array
     *            the array to check
     * @return the duplicate free array
     */
    public static String[] removeDuplicates(String[] array) {
        if (array != null && array.length > 1) {
            // only need to check if there are at least two items
            Set<String> cleanedSet = new HashSet<String>(array.length);
            for (String val : array) {
                cleanedSet.add(val);
            }
            array = new String[cleanedSet.size()];
            int i = 0;
            for (String tag : cleanedSet) {
                array[i++] = tag;
            }
        }
        return array;
    }

    /**
     * Remove duplicate entries in the array. The order to be kept!
     * 
     * @param array
     *            the array to check
     * @param ignoreEmptyStrings
     *            filter empty strings and do not include them in the result array
     * @return the duplicate free array
     */
    public static String[] removeDuplicatesKeepingOrder(String[] array, boolean ignoreEmptyStrings) {
        if (array != null && array.length > 0) {
            // only need to check if there are at least two items
            ArrayList<String> cleanedList = new ArrayList<String>(array.length);

            for (String val : array) {
                if (!ignoreEmptyStrings || StringUtils.isNotBlank(val)) {
                    if (!cleanedList.contains(val)) {
                        cleanedList.add(val);
                    }
                }
            }
            array = new String[cleanedList.size()];
            int i = 0;
            for (String tag : cleanedList) {
                array[i++] = tag;
            }
        }
        return array;
    }

    /**
     * Private constructor
     */
    private ArrayHelper() {

    }
}
