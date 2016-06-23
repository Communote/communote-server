package com.communote.plugins.activity.core;

import org.codehaus.jackson.node.ObjectNode;

import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.core.blog.events.ManagerGainedTopicAccessRightsChangedEvent;
import com.communote.server.service.NoteService;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ManagerGainedTopicAccessRightsChangedEventListener extends
        TopicAccessRightsChangedEventListener<ManagerGainedTopicAccessRightsChangedEvent> {

    /** Prefix for the activity. */
    public static final String TOPIC_GAINED_MANAGEMENT_ACCESS_ACTIVITY_ID =
            "com.communote.core.activity.gained-management-access";

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
    public ManagerGainedTopicAccessRightsChangedEventListener(ActivityService activityService,
            BlogManagement topicManagement, NoteService noteService) {
        super(activityService, topicManagement, noteService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fillTemplateProperties(ObjectNode rootNode,
            ManagerGainedTopicAccessRightsChangedEvent event) {
        rootNode.put("topicId", event.getTopicId());
        rootNode.put("topicTitle", event.getTopicTitle());
        rootNode.put("userId", event.getGrantingUserId());
    }

    /**
     * @return True, if activities have to be created independently of topic settings.
     */
    @Override
    public boolean getForceActivityCreation() {
        return true;
    }

    /**
     * @return ManagerGainedTopicAccessRightsChangedEvent.class
     */
    @Override
    public Class<ManagerGainedTopicAccessRightsChangedEvent> getObservedEvent() {
        return ManagerGainedTopicAccessRightsChangedEvent.class;
    }

    /**
     * {@inheritDoc}
     * 
     * @return {@link #TOPIC_GAINED_MANAGEMENT_ACCESS_ACTIVITY_ID}
     */
    @Override
    public String getTemplate(ManagerGainedTopicAccessRightsChangedEvent event) {
        return TOPIC_GAINED_MANAGEMENT_ACCESS_ACTIVITY_ID;
    }

    /**
     * @return {@value #TOPIC_GAINED_MANAGEMENT_ACCESS_ACTIVITY_ID}
     */
    @Override
    public String getTemplateId() {
        return TOPIC_GAINED_MANAGEMENT_ACCESS_ACTIVITY_ID;
    }

    @Override
    protected boolean isDeletable() {
        return false;
    }

}
