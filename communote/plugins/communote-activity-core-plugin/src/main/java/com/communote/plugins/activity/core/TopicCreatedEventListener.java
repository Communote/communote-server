package com.communote.plugins.activity.core;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.activity.base.processor.ActivityDeactivatedNotePreProcessorException;
import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.blog.BlogCreatedEvent;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.service.NoteService;

/**
 * Creates an activity when a new topic had been created.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a> *
 */
public class TopicCreatedEventListener implements EventListener<BlogCreatedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicCreatedEventListener.class);
    private final ActivityService activityService;
    private final String templateId;
    private NoteService noteManagement;

    /**
     * Create a new listener
     * 
     * @param activityService
     *            the activity service
     * @param templateId
     *            the ID of the activity
     */
    public TopicCreatedEventListener(ActivityService activityService, String templateId) {
        this.activityService = activityService;
        this.templateId = templateId;
    }

    /**
     * Create the JSON object holding the details of the activity
     * 
     * @param event
     *            the event containing the details for building the properties object
     * @return the created JSON
     */
    private String createTemplateProperties(BlogCreatedEvent event) {
        ObjectMapper mapper = JsonHelper.getSharedObjectMapper();
        ObjectNode rootNode = mapper.getNodeFactory().objectNode();
        rootNode.put("topicId", event.getBlogId());
        rootNode.put("topicTitle", event.getTopicTitle());
        rootNode.put("userId", event.getUserId());
        return JsonHelper.writeJsonTreeAsString(rootNode);
    }

    /**
     * 
     * @return the lazily initialized note management
     */
    private NoteService getNoteManagement() {
        if (noteManagement == null) {
            noteManagement = ServiceLocator.instance().getService(NoteService.class);
        }
        return noteManagement;
    }

    @Override
    public Class<BlogCreatedEvent> getObservedEvent() {
        return BlogCreatedEvent.class;
    }

    @Override
    public void handle(BlogCreatedEvent event) {
        // create an activity
        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setBlogId(event.getBlogId());
        noteStoringTO.setCreatorId(event.getUserId());
        noteStoringTO.setPublish(true);
        noteStoringTO.setContentType(NoteContentType.UNKNOWN);
        noteStoringTO.setCreationSource(NoteCreationSource.API);
        activityService.convertToActivityNote(noteStoringTO, this.templateId,
                createTemplateProperties(event));
        try {
            NoteModificationResult result = getNoteManagement().createNote(noteStoringTO, null);
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

}
