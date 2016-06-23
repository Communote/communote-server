package com.communote.common.velocity;

import com.communote.common.string.StringHelper;

/**
 * Tool for handling strings.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class StringTool {

    /**
     * To cut out middle part of a String.
     * 
     * @param string
     *            the string to truncate
     * @param maxLength
     *            the max length of a string
     * @param ellipses
     *            the replacement in case of cutting the string
     * 
     * @return The abbreviated string if the string exceeds the max length otherwise the original
     *         string is returned.
     */
    public String truncateMiddle(String string, int maxLength, String ellipses) {
        return StringHelper.truncateMiddle(string, maxLength, ellipses);
    }
}
