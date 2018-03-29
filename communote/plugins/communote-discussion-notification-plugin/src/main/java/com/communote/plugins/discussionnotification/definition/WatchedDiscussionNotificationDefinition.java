package com.communote.plugins.discussionnotification.definition;

import com.communote.server.core.messaging.NotificationDefinition;

/**
 * Notification definition for watched discussions.
 *
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 */
public class WatchedDiscussionNotificationDefinition extends NotificationDefinition {

    public WatchedDiscussionNotificationDefinition() {
        super("watchedDiscussion");
    }
}
