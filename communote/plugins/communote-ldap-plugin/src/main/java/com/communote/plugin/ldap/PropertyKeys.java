package com.communote.plugin.ldap;

/**
 * Interface containing all property keys for this plugin.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface PropertyKeys {

    /** Property for the ad tracking time out */
    public final static String PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_TIME_OUT = "timeout";

    /** Property for the enabling paging for ad tracking */
    public final static String PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_ALLOW_PAGING = "allow.paging";

    /** Persistent USN */
    public final static String PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_USER_SEQUENCE_NUMBER = "latest.usn";

    /** Property for the ad tracking paging size */
    public final static String PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_PAGING_SIZE = "paging.size";

    /** Property for the enabled status for incremental group synchronization. */
    public final static String PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_ENABLED_GROUP = "incremental.group.enabled";

    /** Property for the enabled status for incremental user synchronization. */
    public final static String PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_ENABLED_USER = "incremental.user.enabled";
}
