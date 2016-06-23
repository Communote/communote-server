package com.communote.server.core.messaging.definitions;

import com.communote.server.core.messaging.NotificationDefinition;

/**
 * Notification definition for discussions.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DiscussionNotificationDefinition extends NotificationDefinition {

    /** Static instance of this definition. */
    public final static NotificationDefinition INSTANCE = new DiscussionNotificationDefinition();

    /**
     * Constructor.
     */
    private DiscussionNotificationDefinition() {
        super("discussion");
    }
}
