package com.communote.plugins.discussionnotification.widget;

import java.util.List;
import java.util.Map;

import com.communote.plugins.discussionnotification.DiscussionNotificationActivator;
import com.communote.plugins.discussionnotification.processor.WatchedDiscussionRenderingPreProcessor;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.web.fe.widgets.extension.WidgetExtension;
import com.communote.server.web.fe.widgets.extension.note.CPLNoteActionsProvider;

/**
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 */
public class WatchDiscussionActionProvider extends CPLNoteActionsProvider {

    private static final String ACTION_NAME = DiscussionNotificationActivator.KEY_GROUP + "-watch";

    @Override
    public boolean addNoteActions(Map<String, String> requestParameters, NoteData data,
            List<String> actions) {
        Boolean watches = data.getProperty(
                WatchedDiscussionRenderingPreProcessor.PROPERTY_KEY_CURRENT_USER_IS_WATCHING);
        if (watches != null) {
            actions.add(ACTION_NAME);
        }
        return true;
    }

    @Override
    public int getOrder() {
        return WidgetExtension.DEFAULT_ORDER_VALUE;
    }

}
