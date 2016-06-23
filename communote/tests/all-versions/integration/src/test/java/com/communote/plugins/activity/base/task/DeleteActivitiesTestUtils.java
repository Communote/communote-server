package com.communote.plugins.activity.base.task;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.UUID;

import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.core.template.NoteTemplateService;
import com.communote.server.model.blog.Blog;
import com.communote.server.service.NoteService;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DeleteActivitiesTestUtils {
    /**
     * Creates and stores an activity for the given template.
     *
     * @param topic
     *            Topic, the activity should be created in.
     * @param authorId
     *            Id of the user, who is the activities author.
     * @param templateId
     *            The template id of the activity.
     * @return Id of the note.
     * @throws Exception
     *             in case the creation of the activity failed
     */
    public static Long createActivity(Blog topic, Long authorId, String templateId)
            throws Exception {
        NoteStoringTO noteStoringTO = createActivityNoteStoringTO(topic, authorId, templateId,
                System.currentTimeMillis());
        return ServiceLocator.instance().getService(NoteService.class)
                .createNote(noteStoringTO, null).getNoteId();
    }

    /**
     * Creates and stores an activity for the given template.
     *
     * @param topic
     *            Topic, the activity should be created in.
     * @param authorId
     *            Id of the user, who is the activities author.
     * @param templateId
     *            The template id of the activity.
     * @param creationDateMillis
     *            Number of milliseconds since epoch which will be used as creation date of the
     *            activity
     * @return Id of the note.
     * @throws Exception
     *             in case the creation of the activity failed
     */
    public static Long createActivity(Blog topic, Long authorId, String templateId,
            long creationDateMillis) throws Exception {
        NoteStoringTO noteStoringTO = createActivityNoteStoringTO(topic, authorId, templateId,
                creationDateMillis);
        return ServiceLocator.instance().getService(NoteService.class)
                .createNote(noteStoringTO, null).getNoteId();
    }

    /**
     * Creates and stores an activity for the given template as a comment to another note.
     *
     * @param parentNoteId
     *            The ID of the parent note.
     * @param topic
     *            Topic, the activity should be created in.
     * @param authorId
     *            Id of the user, who is the activities author.
     * @param templateId
     *            The template id of the activity.
     * @param creationDateMillis
     *            Number of milliseconds since epoch which will be used as creation date of the
     *            activity
     * @return Id of the note.
     * @throws Exception
     *             in case the creation of the activity failed
     */
    public static Long createActivityAsComment(Long parentNoteId, Blog topic, Long authorId,
            String templateId, long creationDateMillis)
                    throws Exception {
        NoteStoringTO noteStoringTO = createActivityNoteStoringTO(topic, authorId, templateId,
                creationDateMillis);
        noteStoringTO.setParentNoteId(parentNoteId);
        return ServiceLocator.instance().getService(NoteService.class)
                .createNote(noteStoringTO, null).getNoteId();
    }

    private static NoteStoringTO createActivityNoteStoringTO(Blog topic, Long authorId,
            String templateId, long creationDateMillis) {
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(topic, authorId, UUID.randomUUID()
                .toString());
        noteStoringTO.setCreationDate(new Timestamp(creationDateMillis));
        HashSet<StringPropertyTO> properties = new HashSet<StringPropertyTO>();
        noteStoringTO.setProperties(properties);

        StringPropertyTO isActivityProperty = new StringPropertyTO();
        isActivityProperty.setKeyGroup(ActivityService.PROPERTY_KEY_GROUP);
        isActivityProperty.setPropertyKey(ActivityService.NOTE_PROPERTY_KEY_ACTIVITY);
        isActivityProperty.setPropertyValue(ActivityService.NOTE_PROPERTY_VALUE_ACTIVITY);

        StringPropertyTO templateIdProperty = new StringPropertyTO();
        templateIdProperty.setKeyGroup(PropertyManagement.KEY_GROUP);
        templateIdProperty.setPropertyKey(NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_ID);
        templateIdProperty.setPropertyValue(templateId);

        properties.add(isActivityProperty);
        properties.add(templateIdProperty);
        return noteStoringTO;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private DeleteActivitiesTestUtils() {
        // Do nothing
    }
}
