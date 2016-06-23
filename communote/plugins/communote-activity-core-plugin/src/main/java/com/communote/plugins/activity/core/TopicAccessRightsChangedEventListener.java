package com.communote.plugins.activity.core;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.converter.Converter;
import com.communote.plugins.activity.base.processor.ActivityDeactivatedNotePreProcessorException;
import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.blog.events.TopicAccessRightsChangedEvent;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.service.NoteService;

/**
 * Abstract event listener for {@link TopicAccessRightsChangedEvent} to ease implementing concrete
 * listeners.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            Concrete to of {@link TopicAccessRightsChangedEvent}
 */
public abstract class TopicAccessRightsChangedEventListener<T extends TopicAccessRightsChangedEvent>
        implements EventListener<T> {

    /** Converter for getting the createSystemNotes flag. */
    private final class IsCreateSystemNotesConverter implements Converter<Blog, Boolean> {
        @Override
        public Boolean convert(Blog source) {
            return source.isCreateSystemNotes();
        }
    }

    /** Prefix for the activity. */
    public static final String TOPIC_ACCESS_RIGHTS_CHANGED_ACTIVITY_ID =
            "com.communote.core.activity.access-rights-changed";

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TopicAccessRightsChangedEventListener.class);
    private final ActivityService activityService;
    private final NoteService noteService;
    private final BlogManagement topicManagement;

    /**
     * Constructor.
     * 
     * @param activityService
     *            The activity service.
     * @param topicManagement
     *            The topic management.
     * @param noteService
     *            The note service.
     */
    public TopicAccessRightsChangedEventListener(ActivityService activityService,
            BlogManagement topicManagement, NoteService noteService) {
        this.activityService = activityService;
        this.topicManagement = topicManagement;
        this.noteService = noteService;
    }

    /**
     * Create the JSON object holding the details of the activity
     * 
     * @param rootNode
     *            The node to add properties to.
     * @param event
     *            the event containing the details for building the properties object
     */
    public abstract void fillTemplateProperties(ObjectNode rootNode, T event);

    /**
     * @return True, if activities have to be created independently of topic settings.
     */
    public boolean getForceActivityCreation() {
        return false;
    }

    /**
     * Method to get the template id.
     * 
     * @param event
     *            The event, which was fired.
     * @return The id of the template to use.
     */
    public abstract String getTemplate(T event);

    /**
     * @return The template id for the created activity definitio, defaults to
     *         {@value #TOPIC_ACCESS_RIGHTS_CHANGED_ACTIVITY_ID}
     */
    public String getTemplateId() {
        return TOPIC_ACCESS_RIGHTS_CHANGED_ACTIVITY_ID;
    }

    /**
     * Returns a set of user alias, which should be notified for this topic access right change.
     * 
     * @param event
     *            The fired event.
     * @return Set of alias, should not return null.
     */
    public Set<String> getUsersToNotify(T event) {
        return new HashSet<String>();
    }

    @Override
    public void handle(T event) {
        try {
            if (!getForceActivityCreation()
                    && !topicManagement.getBlogById(event.getTopicId(),
                            new IsCreateSystemNotesConverter())) {
                return;
            }
        } catch (BlogAccessException e) {
            LOGGER.error("Unexpected exception creating the activity", e);
            return;
        }
        // create an activity
        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setBlogId(event.getTopicId());
        noteStoringTO.setCreatorId(event.getGrantingUserId());
        noteStoringTO.setPublish(true);
        noteStoringTO.setContentType(NoteContentType.UNKNOWN);
        noteStoringTO.setCreationSource(NoteCreationSource.API);
        noteStoringTO.setSendNotifications(true);
        noteStoringTO.setUsersToNotify(getUsersToNotify(event));
        ObjectMapper mapper = JsonHelper.getSharedObjectMapper();
        ObjectNode rootNode = mapper.getNodeFactory().objectNode();
        rootNode.put("template", getTemplate(event));
        fillTemplateProperties(rootNode, event);
        activityService.convertToActivityNote(noteStoringTO,
                getTemplateId(), JsonHelper.writeJsonTreeAsString(rootNode));
        if (!isDeletable()) {
            StringPropertyTO property = new StringPropertyTO();
            property.setKeyGroup(ActivityService.PROPERTY_KEY_GROUP);
            property.setPropertyKey(ActivityService.NOTE_PROPERTY_KEY_ACTIVITY_UNDELETABLE);
            property.setPropertyValue(Boolean.TRUE.toString());
            noteStoringTO.getProperties().add(property);
        }
        try {
            NoteModificationResult result = noteService.createNote(noteStoringTO, null);
            if (LOGGER.isDebugEnabled()) {
                if (result.getStatus().equals(NoteModificationStatus.SUCCESS)) {
                    LOGGER.debug("Create topic activity note (" + result.getNoteId()
                            + ") was created successfully");
                } else {
                    LOGGER.debug("Creating create topic activity note failed: "
                            + result.getStatus().name());
                }
            }
        } catch (BlogNotFoundException e) {
            LOGGER.error("Unexpected exception creating the activity", e);
        } catch (NoteManagementAuthorizationException e) {
            LOGGER.error("Unexpected exception creating the activity", e);
        } catch (ActivityDeactivatedNotePreProcessorException e) {
            LOGGER.debug(e.getMessage());
        } catch (NoteStoringPreProcessorException e) {
            LOGGER.error("Unexpected exception creating the activity", e);
        }
    }

    /**
     * @return True, if this activity can be deleted.
     */
    protected boolean isDeletable() {
        return true;
    }
}
