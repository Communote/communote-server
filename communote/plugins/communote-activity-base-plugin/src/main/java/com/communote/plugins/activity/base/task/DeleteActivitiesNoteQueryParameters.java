package com.communote.plugins.activity.base.task;

import java.util.Date;

import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.core.template.NoteTemplateService;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.filter.PropertyFilter;
import com.communote.server.core.vo.query.filter.PropertyFilter.MatchMode;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.note.Note;

/**
 * {@link NoteQueryParameters} with additional fields for retrieving only activities but excluding
 * those that cannot be deleted.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DeleteActivitiesNoteQueryParameters extends NoteQueryParameters {

    private static final PropertyFilter ACTIVITY_PROPERTY_FILTER = new PropertyFilter(
            ActivityService.PROPERTY_KEY_GROUP, Note.class);

    private static final PropertyFilter ACTIVITY_NEGATED_PROPERTY_FILTER = new PropertyFilter(
            ActivityService.PROPERTY_KEY_GROUP, Note.class, true);
    static {
        ACTIVITY_PROPERTY_FILTER.addProperty(ActivityService.NOTE_PROPERTY_KEY_ACTIVITY,
                ActivityService.NOTE_PROPERTY_VALUE_ACTIVITY, MatchMode.EQUALS);
        ACTIVITY_NEGATED_PROPERTY_FILTER.addProperty(
                ActivityService.NOTE_PROPERTY_KEY_ACTIVITY_UNDELETABLE,
                Boolean.TRUE.toString(), MatchMode.EXISTS);
    }

    /**
     * Constructor.
     * 
     * @param templateId
     *            Id of the template, the activities to find should be.
     * @param expirationTimeoutInMillis
     *            See
     *            {@link com.communote.plugins.activity.base.data.ActivityConfiguration#getExpirationTimeout()}
     */
    public DeleteActivitiesNoteQueryParameters(String templateId, long expirationTimeoutInMillis) {
        addPropertyFilter(ACTIVITY_PROPERTY_FILTER);
        addPropertyFilter(ACTIVITY_NEGATED_PROPERTY_FILTER);
        PropertyFilter templateIdPropertyFilter = new PropertyFilter(
                PropertyManagement.KEY_GROUP, Note.class);
        templateIdPropertyFilter.addProperty(NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_ID,
                templateId, MatchMode.EQUALS);
        addPropertyFilter(templateIdPropertyFilter);

        TaggingCoreItemUTPExtension extension = new TaggingCoreItemUTPExtension();
        extension.setTopicAccessLevel(TopicAccessLevel.SYSTEM);
        setTypeSpecificExtension(extension);
        setUpperTagDate(new Date(System.currentTimeMillis() - expirationTimeoutInMillis));
    }

}
