package com.communote.plugins.activity.core;

import org.codehaus.jackson.node.ObjectNode;

import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.core.blog.events.AllUsersTopicAccessRightsChangedEvent;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.service.NoteService;

/**
 * Creates an activity when a new topic had been created.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a> *
 */
public class AllUsersTopicAccessRightsChangedEventListener extends
        TopicAccessRightsChangedEventListener<AllUsersTopicAccessRightsChangedEvent> {

    /**
     * Create a new listener
     * 
     * @param activityService
     *            the activity service
     * @param topicManagement
     *            The topic management.
     * @param noteService
     *            The note service.
     */
    public AllUsersTopicAccessRightsChangedEventListener(ActivityService activityService,
            BlogManagement topicManagement, NoteService noteService) {
        super(activityService, topicManagement, noteService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fillTemplateProperties(ObjectNode rootNode,
            AllUsersTopicAccessRightsChangedEvent event) {
        rootNode.put("topicId", event.getTopicId());
        rootNode.put("topicTitle", event.getTopicTitle());
        rootNode.put("userId", event.getGrantingUserId());
        if (event.getOldRole() != null) {
            rootNode.put("oldRole", event.getOldRole().getValue());
        }
        if (event.getNewRole() != null) {
            rootNode.put("newRole", event.getNewRole().getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<AllUsersTopicAccessRightsChangedEvent> getObservedEvent() {
        return AllUsersTopicAccessRightsChangedEvent.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTemplate(AllUsersTopicAccessRightsChangedEvent event) {
        if (event.getNewRole() == null) {
            return "remove.global";
        } else if (BlogRole.MEMBER.equals(event.getNewRole())) {
            return "add.global.write";
        }
        return "add.global.read";
    }
}
