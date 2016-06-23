package com.communote.common.util;

/**
 * Helper class for numbers
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class NumberHelper {

    /**
     * Compare the two numbers and taking care of null values
     * 
     * @param n1
     *            number one
     * @param n2
     *            number two
     * @return if the number are equals
     */
    public static boolean equals(Number n1, Number n2) {
        if (n1 == n2) {
            return true;
        }
        if (n1 != null) {
            return n1.equals(n2);
        }
        return false;
    }

    /**
     * Returns an array containing the start and the end of the range the provided number is in. All
     * ranges have a size of rangeSize and the first starts with 0.
     * 
     * @param number
     *            the number whose range is searched
     * @param rangeSize
     *            the size of the ranges
     * @return an array whose first member is the start of the range and the 2nd is the end
     */
    public static long[] getRangeOfNumber(long number, long rangeSize) {
        long rangeStart = (number / rangeSize) * rangeSize;
        return new long[] { rangeStart, rangeStart + rangeSize - 1 };
    }

    /**
     * do not use me
     */
    private NumberHelper() {

    }
}
