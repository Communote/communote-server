package com.communote.server.core.messaging.definitions;

import com.communote.server.core.messaging.NotificationDefinition;

/**
 * Notification definition for likes.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LikeNotificationDefinition extends NotificationDefinition {

    /** Static instance of this definition. */
    public final static NotificationDefinition INSTANCE = new LikeNotificationDefinition();

    /**
     * Constructor.
     */
    private LikeNotificationDefinition() {
        super("like");
    }

}
