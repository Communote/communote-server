package com.communote.server.core.messaging;

/**
 * Interface with common constants for Messaging Management.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface NotificationManagementConstants {
    /** Default priority for XMPP messages */
    public static final int XMPP_PRIORITY = 2;
    /** Default priority for Mail messages */
    public static final int MAIL_PRIORITY = 1;
    /** Property for fallback on fail. */
    public final static String FALLBACK_ON_FAIL_LITERAL = "fallback";
}
