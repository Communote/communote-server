package com.communote.server.api.core.config.database;

/**
 * Date field for the date part function.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum DateField {
    /**
     * For the day of the date
     */
    DAY,
    /**
     * For the day of the year
     */
    DOY,
    /**
     * For the week of the year
     */
    WEEK,
    /**
     * For the month of the date
     */
    MONTH,
    /**
     * For the year of the date
     */
    YEAR;
}