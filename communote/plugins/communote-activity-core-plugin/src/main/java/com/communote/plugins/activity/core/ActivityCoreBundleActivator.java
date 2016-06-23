package com.communote.plugins.activity.core;

import java.util.ArrayList;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import com.communote.plugins.activity.base.data.ActivityDefinition;
import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.event.Event;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;
import com.communote.server.service.NoteService;

/**
 * iPOJO style activator that registers the activity definitions
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Instantiate
public class ActivityCoreBundleActivator {

    private static final String CREATE_TOPIC_ACTIVITY_ID = "com.communote.core.activity.topic_created";

    @Requires
    private ActivityService activityService;

    private final EventDispatcher eventDispatcher = ServiceLocator.instance().getService(
            EventDispatcher.class);

    private ArrayList<ActivityDefinition> activities;

    private final ArrayList<EventListener<? extends Event>> eventListeners =
            new ArrayList<EventListener<? extends Event>>();

    /**
     * Create the activity definitions if not yet existing
     */
    private void createActivities() {
        if (activities != null) {
            return;
        }
        activities = new ArrayList<ActivityDefinition>();
        ActivityDefinition activity = new ActivityDefinition(
                CREATE_TOPIC_ACTIVITY_ID, new MessageKeyLocalizedMessage(
                        CREATE_TOPIC_ACTIVITY_ID + ".name"),
                new MessageKeyLocalizedMessage(
                        CREATE_TOPIC_ACTIVITY_ID + ".template"), false, false);
        activities.add(activity);
        activity = new ActivityDefinition(
                TopicAccessRightsChangedEventListener.TOPIC_ACCESS_RIGHTS_CHANGED_ACTIVITY_ID,
                new MessageKeyLocalizedMessage(
                        TopicAccessRightsChangedEventListener.TOPIC_ACCESS_RIGHTS_CHANGED_ACTIVITY_ID
                                + ".name"),
                new MessageKeyLocalizedMessage(
                        TopicAccessRightsChangedEventListener.TOPIC_ACCESS_RIGHTS_CHANGED_ACTIVITY_ID
                                + ".template"), false, true);
        activities.add(activity);
        activity = new ActivityDefinition(
                TopicHierarchyEventListener.TEMPLATE_ID,
                new MessageKeyLocalizedMessage(
                        TopicHierarchyEventListener.TEMPLATE_ID
                                + ".name"),
                new MessageKeyLocalizedMessage(
                        TopicHierarchyEventListener.TEMPLATE_ID
                                + ".template"), false, true);
        activities.add(activity);
        activity = new ActivityDefinition(
                ManagerGainedTopicAccessRightsChangedEventListener.TOPIC_GAINED_MANAGEMENT_ACCESS_ACTIVITY_ID,
                false, false, false);
        activity.setDeletableByUser(false);
        activity.setDeletable(false);
        activities.add(activity);
    }

    /**
     * Register all event listeners and store references for removal
     */
    private void registerEventListeners() {
        EventListener<? extends Event> listener = new TopicCreatedEventListener(activityService,
                CREATE_TOPIC_ACTIVITY_ID);
        eventDispatcher.register(listener);
        eventListeners.add(listener);
        NoteService noteService = ServiceLocator.instance().getService(NoteService.class);
        BlogManagement topicManagement = ServiceLocator.findService(BlogManagement.class);
        listener = new AllUsersTopicAccessRightsChangedEventListener(activityService,
                topicManagement, noteService);
        eventDispatcher.register(listener);
        eventListeners.add(listener);
        listener = new EntityTopicAccessRightsChangedEventListener(activityService,
                topicManagement, noteService, ServiceLocator.findService(UserGroupManagement.class));
        eventDispatcher.register(listener);
        listener = new ManagerGainedTopicAccessRightsChangedEventListener(activityService,
                topicManagement, noteService);
        eventDispatcher.register(listener);
        listener = new TopicHierarchyEventListener(activityService, noteService);
        eventDispatcher.register(listener);
        eventListeners.add(listener);
    }

    /**
     * registers the activities
     */
    @Validate
    public void start() {
        createActivities();
        for (ActivityDefinition activity : activities) {
            activityService.addDefinition(activity);
        }
        registerEventListeners();
    }

    /**
     * removes the activities
     */
    @Invalidate
    public void stop() {
        // activityService might be or get invalid -> remove all
        for (EventListener<? extends Event> listener : eventListeners) {
            eventDispatcher.unregister(listener);
        }
        eventListeners.clear();
        if (activityService != null) {
            for (ActivityDefinition activity : activities) {
                activityService.removeDefinition(activity.getTemplateId());
            }
        }
    }

}
