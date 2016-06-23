package com.communote.plugins.activity.base.fe.widget;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.web.fe.widgets.extension.WidgetExtension;
import com.communote.server.web.fe.widgets.extension.note.CPLNoteItemTemplateProvider;
import com.communote.server.web.fe.widgets.extension.note.CPLNoteItemTemplateProviderManagement;


/**
 * Returns a specific template when a note is an activity.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "ActivityNoteItemTemplateProvider")
@Provides
// note: explicitly adding the interface here otherwise Provides would fail while loading the
// interface
public class ActivityNoteItemTemplateProvider extends CPLNoteItemTemplateProvider implements
        WidgetExtension<CPLNoteItemTemplateProvider, CPLNoteItemTemplateProviderManagement> {

    @Requires
    private ActivityService activityService;

    @Override
    public String getNoteItemTemplate(NoteData data) {
        if (isActivity(data)) {
            return "core.widget.blog.post.list.activity.note";
        }
        return null;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER_VALUE;
    }

    /**
     * Test if the note list item is an activity
     * 
     * @param item
     *            the note list data
     * 
     * @return true if it is one false otherwise
     */
    private boolean isActivity(NoteData item) {
        if (item.getObjectProperties() != null) {
            String keyGroup = ActivityService.PROPERTY_KEY_GROUP;
            for (StringPropertyTO property : item.getObjectProperties()) {
                if (property.getKeyGroup().equals(keyGroup)
                        && property.getPropertyKey().equals(
                                ActivityService.NOTE_PROPERTY_KEY_ACTIVITY)
                        && property.getPropertyValue().equals(
                                ActivityService.NOTE_PROPERTY_VALUE_ACTIVITY)) {
                    return true;
                }
            }
        }
        return false;
    }

}
