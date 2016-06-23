package com.communote.server.core.common.time;

/**
 * A simplified version of a {@link java.util.TimeZone}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class SimplifiedTimeZone {
    /**
     * The message key of a time zone id
     */
    private String messageKey;

    /**
     * A java time zone id
     */
    private String timeZoneId;

    /**
     * The amount of time in milliseconds to add to UTC to get standard time in this time zone. This
     * value is not affected by daylight saving time.
     */
    private int rawOffset;

    /**
     * This name is used to sort the time zone list.
     */
    private String sortingName;

    /**
     *
     *
     * @param messageKey
     *            The message key
     * @param timeZoneId
     *            The ID of the {@link java.util.TimeZone} instance this simplified time zone
     *            belongs to
     * @param rawOffset
     *            the amount of time in milliseconds to add to UTC to get standard time in this time
     *            zone
     */
    public SimplifiedTimeZone(String messageKey, String timeZoneId, int rawOffset, String sortingName) {

        this.messageKey = messageKey;
        this.timeZoneId = timeZoneId;
        this.rawOffset = rawOffset;
        this.sortingName = sortingName;
    }

    /**
     * @return the messageKey
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * @return the amount of time in milliseconds to add to UTC to get standard time in this time
     *         zone. Because this value is not affected by daylight saving time, it is called raw
     *         offset.
     */
    public int getRawOffset() {
        return rawOffset;
    }

    /**
     * @return the sortingName
     */
    public String getSortingName() {
        return sortingName;
    }

    /**
     * @return the ID of the {@link java.util.TimeZone} instance this simplified time zone belongs
     *         to
     */
    public String getTimeZoneId() {
        return timeZoneId;
    }

    /**
     * @param messageKey
     *            the messageKey for localizing this time zone instance
     */
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * @param rawOffset
     *            the amount of time in milliseconds to add to UTC to get standard time in this time
     *            zone.
     */
    public void setRawOffset(int rawOffset) {
        this.rawOffset = rawOffset;
    }

    /**
     * @param sortingName
     *            the sortingName to set
     */
    public void setSortingName(String sortingName) {
        this.sortingName = sortingName;
    }

    /**
     * @param timeZoneId
     *            the ID of the {@link java.util.TimeZone} instance this simplified time zone
     *            belongs to
     */
    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }
}
