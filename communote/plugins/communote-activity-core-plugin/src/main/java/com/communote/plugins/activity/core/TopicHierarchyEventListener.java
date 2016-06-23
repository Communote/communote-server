package com.communote.plugins.activity.core;

import java.sql.Timestamp;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

import com.communote.plugins.activity.base.processor.ActivityDeactivatedNotePreProcessorException;
import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.blog.TopicHierarchyEvent;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.service.NoteService;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicHierarchyEventListener implements EventListener<TopicHierarchyEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(TopicHierarchyEventListener.class);

    private final ActivityService activityService;
    private final NoteService noteService;
    /** Id of the activities template. */
    public final static String TEMPLATE_ID = "com.communote.core.activity.topic_hierarchy";

    /**
     * Constructor.
     * 
     * @param activityService
     *            The activity service to use.
     * @param noteService
     *            The note service to use.
     */
    public TopicHierarchyEventListener(ActivityService activityService, NoteService noteService) {
        this.activityService = activityService;
        this.noteService = noteService;
    }

    /**
     * This method finally creates the activity.
     * 
     * @param topicId
     *            Topic for the activity.
     * @param authorId
     *            Author of the activity.
     * @param templateProperties
     *            Properties of the activity.
     */
    private void createActivity(Long topicId, Long authorId, String templateProperties) {
        if (topicId == null) {
            return; // This might be a removed topic.
        }
        // create an activity
        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setBlogId(topicId);
        noteStoringTO.setCreatorId(authorId);
        noteStoringTO.setPublish(true);
        noteStoringTO.setContentType(NoteContentType.UNKNOWN);
        noteStoringTO.setCreationSource(NoteCreationSource.API);
        noteStoringTO.setCreationDate(new Timestamp(System.currentTimeMillis() + 1000));
        activityService.convertToActivityNote(noteStoringTO, TEMPLATE_ID, templateProperties);
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
     * Create the JSON object holding the details of the activity
     * 
     * @param event
     *            the event containing the details for building the properties object
     * @return the created JSON
     */
    private String createTemplateProperties(TopicHierarchyEvent event) {
        ObjectMapper mapper = JsonHelper.getSharedObjectMapper();
        ObjectNode rootNode = mapper.getNodeFactory().objectNode();
        rootNode.put("parentTopicId", event.getParentTopicId());
        rootNode.put("parentTopicTitle", event.getParentTopicTitle());
        rootNode.put("childTopicId", event.getChildTopicId());
        rootNode.put("childTopicTitle", event.getChildTopicTitle());
        rootNode.put("method", event.getType().name());
        rootNode.put("userId", event.getUserId());
        return JsonHelper.writeJsonTreeAsString(rootNode);
    }

    @Override
    public Class<TopicHierarchyEvent> getObservedEvent() {
        return TopicHierarchyEvent.class;
    }

    @Override
    public void handle(TopicHierarchyEvent event) {
        SecurityContext currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
        try {
            String templateProperties = createTemplateProperties(event);
            createActivity(event.getChildTopicId(), event.getUserId(), templateProperties);
        } finally {
            AuthenticationHelper.setSecurityContext(currentContext);
        }
    }
}
