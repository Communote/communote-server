package com.communote.plugins.discussionnotification.definition;

import com.communote.server.core.messaging.NotificationDefinition;

/**
 * Notification definition for discussion participations.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DiscussionParticipationNotificationDefinition extends NotificationDefinition {

    /**
     * Constructor.
     */
    public DiscussionParticipationNotificationDefinition() {
        super("discussion");
    }
}
