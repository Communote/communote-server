package com.communote.plugins.discussionnotification.widget;

import java.util.Map;

import com.communote.plugins.discussionnotification.processor.WatchedDiscussionRenderingPreProcessor;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.web.fe.widgets.extension.WidgetExtension;
import com.communote.server.web.fe.widgets.extension.note.CPLNoteMetaDataProvider;

/**
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 */
public class WatchDiscussionMetadataProvider extends CPLNoteMetaDataProvider {

    @Override
    public void addMetaData(Map<String, String> requestParameters, NoteData data,
            Map<String, Object> metaData) {
        Boolean watches = data.getProperty(
                WatchedDiscussionRenderingPreProcessor.PROPERTY_KEY_CURRENT_USER_IS_WATCHING);
        if (watches != null) {
            metaData.put(
                    WatchedDiscussionRenderingPreProcessor.PROPERTY_KEY_CURRENT_USER_IS_WATCHING,
                    watches);
        }

    }

    @Override
    public int getOrder() {
        return WidgetExtension.DEFAULT_ORDER_VALUE;
    }

}
