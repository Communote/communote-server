package com.communote.server.core.messaging;

/**
 * Enum containing possible values, for when a notification should occur.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum NotificationScheduleTypes {
    /** The notification will never be send. */
    NEVER,
    /** The notification will be send immediately, when it occurs. */
    IMMEDIATE,
    /** The notification will be send daily within a digest. */
    DAILY,
    /** The notification will be send weekly within a digest. */
    WEEKLY
}
