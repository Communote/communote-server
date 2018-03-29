package com.communote.plugins.discussionnotification.i18n;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.server.web.commons.i18n.JsMessagesExtension;
import com.communote.server.web.commons.i18n.JsMessagesRegistry;

/**
 * Declare message keys to be exported to JavaScript frontend
 *
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 */
@Component
@Provides
@Instantiate
public class DiscussionNotificationJsMessages implements JsMessagesExtension {

    @Override
    public Map<String, Set<String>> getJsMessageKeys() {
        HashSet<String> messageKeys = new HashSet<String>();
        messageKeys.add(
                "widget.chronologicalPostList.note.action.communote_discussion_notification_plugin-watch.label");
        messageKeys.add(
                "widget.chronologicalPostList.note.action.communote_discussion_notification_plugin-watch.title");

        HashMap<String, Set<String>> mapping = new HashMap<String, Set<String>>();
        mapping.put(JsMessagesRegistry.CATEGORY_PORTAL, messageKeys);
        return mapping;
    }

}
