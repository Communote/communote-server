package com.communote.server.core.messaging.definitions;

import com.communote.server.core.messaging.NotificationDefinition;

/**
 * Notification definition for mentions.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MentionNotificationDefinition extends NotificationDefinition {

    /** Static instance of this definition. */
    public final static MentionNotificationDefinition INSTANCE = new MentionNotificationDefinition();

    /**
     * Constructor.
     */
    private MentionNotificationDefinition() {
        super("mention");
    }

}
