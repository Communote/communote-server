package com.communote.server.core.common;

import java.text.DecimalFormat;

/**
 * Helper class which offers basic methods for the limit management.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class LimitHelper {

    /**
     * Gets the count as percent.
     *
     * @param count
     *            the count
     * @param limit
     *            the limit
     * @return the count percent
     */
    public static float getCount(long count, long limit) {
        float result = 0F;
        if (limit > 0) {
            result = Float.valueOf(count) / Float.valueOf(limit);
        }
        return result;
    }

    /**
     * Gets the count limit as string.
     *
     * @param limit
     *            the limit
     * @return the count limit as string
     */
    public static String getCountLimitAsString(long limit) {
        String result;
        if (limit > 0) {
            result = String.valueOf(limit);
        } else {
            result = "unlimited";
        }
        return result;
    }

    /**
     * Gets the count as percent.
     *
     * @param count
     *            the count
     * @param limit
     *            the limit
     * @return the count percent
     */
    public static float getCountPercent(long count, long limit) {
        return getCount(count, limit) * 100;
    }

    /**
     * Gets the percent as string.
     *
     * @param count
     *            the count
     * @param limit
     *            the limit
     * @return the count percent as string including the '%' character
     */
    public static String getCountPercentAsString(long count, long limit) {
        return new DecimalFormat("##.##%").format(getCount(count, limit));
    }

    /**
     * Check whether the count value has or exceeds the limit.
     *
     * @param count
     *            the count
     * @param limit
     *            the limit to check against. A value of 0 or less is interpreted as no limit.
     * @return true, if the limit is reached or exceeded.
     */
    public static boolean isCountLimitReached(long count, long limit) {
        return limit > 0 && count >= limit;
    }

    /**
     * Instantiates a new limit helper.
     */
    private LimitHelper() {
    }
}
