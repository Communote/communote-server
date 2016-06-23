package com.communote.common.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Date helper
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DateHelper {

    /**
     * a milli seconds
     */
    public final static long MILLI_SECONDS = 1;

    /**
     * a second in milli seconds
     */
    public final static long SECONDS = 1000 * MILLI_SECONDS;

    /**
     * a second in seconds
     */
    public final static int SECONDS_IN_SEC = 1;

    /**
     * a minute in milli seconds
     */
    public final static long MINUTES = 60 * SECONDS;

    /**
     * a minutes in seconds
     */
    public final static int MINUTES_IN_SEC = 60 * SECONDS_IN_SEC;

    /**
     * an hour in milli seconds
     */
    public final static long HOURS = 60 * MINUTES;

    /**
     * a hour in seconds
     */
    public final static int HOURS_IN_SEC = 60 * MINUTES_IN_SEC;

    /**
     * a day in milli seconds
     */
    public final static long DAYS = 24 * HOURS;

    /**
     * a day in seconds
     */
    public final static int DAYS_IN_SEC = 24 * HOURS_IN_SEC;

    /**
     * Check if the calendars are referring to the same day
     * 
     * @param dayOne
     *            the day one
     * @param dayTwo
     *            the day two
     * @return true if the days are equal
     */
    public static boolean isTheSameDay(Calendar dayOne, Calendar dayTwo) {
        boolean result = false;
        if (dayOne == null || dayTwo == null) {
            result = false;
        } else {
            result = (dayTwo.get(Calendar.DAY_OF_YEAR) == dayOne.get(Calendar.DAY_OF_YEAR))
                    && (dayTwo.get(Calendar.YEAR) == dayOne.get(Calendar.YEAR));
        }
        return result;
    }

    /**
     * Check if the dates are referring to the same day
     * 
     * @param dayOne
     *            the day one
     * @param dayTwo
     *            the day two
     * @return true if the days are equal
     */
    public static boolean isTheSameDay(Date dayOne, Date dayTwo) {
        return isTheSameDay(dayOne, dayTwo, null);
    }

    /**
     * Check if the dates are referring to the same day and add set the timezone
     * 
     * @param dayOne
     *            the day one with respect to the timezone
     * @param dayTwo
     *            the day two with respect to the timezone
     * @param userTimeZone
     *            for timezone specific comparison; can be null
     * @return true if the days are equal
     */

    public static boolean isTheSameDay(Date dayOne, Date dayTwo, TimeZone userTimeZone) {

        Calendar one = new GregorianCalendar();
        Calendar two = new GregorianCalendar();

        one.setTime(dayOne);
        two.setTime(dayTwo);
        if (userTimeZone != null) {
            one.setTimeZone(userTimeZone);
            two.setTimeZone(userTimeZone);
        }
        return isTheSameDay(one, two);
    }

    /**
     * Check if the dates are referring to the same day
     * 
     * @param day
     *            the day one
     * @return true if the days are equal
     */
    public static boolean isToday(Date day) {
        return isTheSameDay(day, new Date());

    }

    /**
     * Use as <code>nowBefore(14*DateHelper.DAYS)</code>
     * 
     * @param time
     *            the time to subtract from now
     * @return now before the given time
     */
    public static long nowBefore(long time) {
        return (new Date().getTime() - time);
    }

    /**
     * Subtract the time from now and reset the time of the given day
     * 
     * @param time
     *            the time to subtract from now
     * @return the date in milliseconds from today minus the date given but set to midnight of the
     *         same day
     */
    public static long nowBeforeTrimToMidnight(long time) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date(nowBefore(time)));
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.AM_PM, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();

    }

    /**
     * Use as <code>nowBeforeXMonths(1)</code> to get the time of today before one month
     * 
     * @param x
     *            the number of months to substract from today
     * @return now before x months
     */
    public static long nowBeforeXMonths(int x) {
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.MONTH, -x);
        return cal.getTimeInMillis();
    }

    /**
     * do not construct me
     */
    private DateHelper() {
    }
}
