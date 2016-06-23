package com.communote.plugins.activity.core;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.node.ObjectNode;

import com.communote.common.converter.Converter;
import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.core.blog.events.EntityTopicAccessRightsChangedEvent;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.group.Group;
import com.communote.server.service.NoteService;

/**
 * Creates an activity when a new topic had been created.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a> *
 */
public class EntityTopicAccessRightsChangedEventListener extends
        TopicAccessRightsChangedEventListener<EntityTopicAccessRightsChangedEvent> {

    private final static Map<BlogRole, String> ROLE_TO_STRING_MAPPING = new HashMap<BlogRole, String>();
    private final UserGroupManagement userGroupManagement;
    {
        ROLE_TO_STRING_MAPPING.put(BlogRole.MANAGER, "manager");
        ROLE_TO_STRING_MAPPING.put(BlogRole.VIEWER, "reader");
        ROLE_TO_STRING_MAPPING.put(BlogRole.MEMBER, "author");
    }

    /**
     * Create a new listener
     * 
     * @param activityService
     *            the activity service
     * @param topicManagement
     *            The topic management.
     * @param noteService
     *            The note service
     * @param userGroupManagement
     *            User group management to use.
     */
    public EntityTopicAccessRightsChangedEventListener(ActivityService activityService,
            BlogManagement topicManagement, NoteService noteService,
            UserGroupManagement userGroupManagement) {
        super(activityService, topicManagement, noteService);
        this.userGroupManagement = userGroupManagement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fillTemplateProperties(ObjectNode rootNode,
            EntityTopicAccessRightsChangedEvent event) {
        rootNode.put("topicId", event.getTopicId());
        rootNode.put("topicTitle", event.getTopicTitle());
        rootNode.put("userId", event.getGrantingUserId());
        rootNode.put("isGroup", event.isGroup());
        if (event.getOldRole() != null) {
            rootNode.put("oldRole", event.getOldRole().getValue());
        }
        if (event.getNewRole() != null) {
            rootNode.put("newRole", event.getNewRole().getValue());
        }
        if (event.isGroup()) {
            rootNode.put("groupName", userGroupManagement.findGroupById(event.getGrantedEntityId(),
                    new Converter<Group, String>() {
                        @Override
                        public String convert(Group source) {
                            return source.getName();
                        }
                    }));
        } else {
            rootNode.put("grantedUserId", event.getGrantedEntityId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<EntityTopicAccessRightsChangedEvent> getObservedEvent() {
        return EntityTopicAccessRightsChangedEvent.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTemplate(EntityTopicAccessRightsChangedEvent event) {
        BlogRole oldRole = event.getOldRole();
        BlogRole newRole = event.getNewRole();
        if (newRole == null) {
            return "remove." + ROLE_TO_STRING_MAPPING.get(oldRole);
        }
        return "add." + ROLE_TO_STRING_MAPPING.get(newRole);
    }
}
