package com.communote.plugins.discussionnotification;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.communote.plugins.discussionnotification.definition.DiscussionParticipationNotificationDefinition;
import com.communote.plugins.discussionnotification.definition.WatchedDiscussionNotificationDefinition;
import com.communote.plugins.discussionnotification.processor.DiscussionParticipationNotificationNoteProcessor;
import com.communote.plugins.discussionnotification.processor.WatchedDiscussionNotificationNoteProcessor;
import com.communote.plugins.discussionnotification.processor.WatchedDiscussionRenderingPreProcessor;
import com.communote.plugins.discussionnotification.widget.WatchDiscussionActionProvider;
import com.communote.plugins.discussionnotification.widget.WatchDiscussionMetadataProvider;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorManager;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorManager;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.core.blog.notes.processors.DiscussionNotificationNoteProcessor;
import com.communote.server.core.blog.notes.processors.NotificationNoteProcessor;
import com.communote.server.core.messaging.NotificationDefinition;
import com.communote.server.core.messaging.NotificationService;
import com.communote.server.service.NoteService;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.fe.widgets.extension.WidgetExtension;
import com.communote.server.web.fe.widgets.extension.WidgetExtensionManagementRepository;

/**
 * Activator which registers the notification definitions, processors and properties.
 *
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 */
public class DiscussionNotificationActivator implements BundleActivator {

    public static final String KEY_GROUP = "communote_discussion_notification_plugin";
    /**
     * Key of the user note property holding whether a user watches a discussion or doesn't watch it
     * anymore.
     */
    public static final String PROPERTY_KEY_WATCHED_DISCUSSION = "discussion.watched";

    private WatchedDiscussionNotificationDefinition watchedNotificationDefinition;
    private NotificationNoteProcessor watchedNotificationProcessor;
    private DiscussionParticipationNotificationDefinition participationNotificationDefinition;
    private NotificationNoteProcessor participtionNotificationProcessor;
    private WatchedDiscussionRenderingPreProcessor watchedDiscussionRenderingPreProcessor;
    private WatchDiscussionActionProvider watchDiscussionActionProvider;
    private WatchDiscussionMetadataProvider watchDiscussionMetadataProvider;

    private NoteRenderingPreProcessorManager getNoteRenderingPreProcessorManager() {
        return ServiceLocator.findService(NoteRenderingPreProcessorManager.class);
    }

    private NoteStoringPostProcessorManager getNoteStoringPostProcessorManager() {
        return ServiceLocator.findService(NoteStoringPostProcessorManager.class);
    }

    private NotificationService getNotificationService() {
        return ServiceLocator.findService(NotificationService.class);
    }

    private WidgetExtensionManagementRepository getWidgetExtensionManagementRepository() {
        return WebServiceLocator.findService(WidgetExtensionManagementRepository.class);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        PropertyManagement propertyManagement = ServiceLocator
                .findService(PropertyManagement.class);
        propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty,
                DiscussionNotificationActivator.KEY_GROUP, PROPERTY_KEY_WATCHED_DISCUSSION);
        // register notification definitions
        watchedNotificationDefinition = new WatchedDiscussionNotificationDefinition();
        participationNotificationDefinition = new DiscussionParticipationNotificationDefinition();
        NotificationService notificationService = getNotificationService();
        notificationService.register(watchedNotificationDefinition,
                participationNotificationDefinition);
        // register notification processors
        BlogRightsManagement topicRightsManagement = ServiceLocator
                .findService(BlogRightsManagement.class);
        NoteStoringPostProcessorManager postProcessorManager = getNoteStoringPostProcessorManager();
        watchedNotificationProcessor = new WatchedDiscussionNotificationNoteProcessor(
                propertyManagement, topicRightsManagement, watchedNotificationDefinition);
        postProcessorManager.addProcessor(watchedNotificationProcessor);
        participtionNotificationProcessor = new DiscussionParticipationNotificationNoteProcessor(
                Boolean.getBoolean(DiscussionNotificationNoteProcessor.PROPERTY_PARENT_TREE_ONLY),
                topicRightsManagement, propertyManagement, participationNotificationDefinition);
        postProcessorManager.addProcessor(participtionNotificationProcessor);
        // register rendering preprocessor
        watchedDiscussionRenderingPreProcessor = new WatchedDiscussionRenderingPreProcessor(
                ServiceLocator.findService(NoteService.class), notificationService,
                propertyManagement, watchedNotificationDefinition,
                participationNotificationDefinition);
        getNoteRenderingPreProcessorManager().addProcessor(watchedDiscussionRenderingPreProcessor);
        // register CPL widget extensions
        WidgetExtensionManagementRepository extensionRepo = getWidgetExtensionManagementRepository();
        watchDiscussionActionProvider = new WatchDiscussionActionProvider();
        // forced cast with raw type because Java 7 doesn't resolve the bound types correctly
        extensionRepo.addExtension((WidgetExtension) watchDiscussionActionProvider);
        watchDiscussionMetadataProvider = new WatchDiscussionMetadataProvider();
        extensionRepo.addExtension((WidgetExtension) watchDiscussionMetadataProvider);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        PropertyManagement propertyManagement = ServiceLocator
                .findService(PropertyManagement.class);
        propertyManagement.removeObjectPropertyFilter(PropertyType.UserNoteProperty,
                DiscussionNotificationActivator.KEY_GROUP, PROPERTY_KEY_WATCHED_DISCUSSION);

        NotificationService notificationService = getNotificationService();
        unregisterDefinition(notificationService, participationNotificationDefinition);
        unregisterDefinition(notificationService, watchedNotificationDefinition);
        NoteStoringPostProcessorManager postProcessorManager = getNoteStoringPostProcessorManager();
        unregisterManager(postProcessorManager, participtionNotificationProcessor);
        unregisterManager(postProcessorManager, watchedNotificationProcessor);
        if (watchedDiscussionRenderingPreProcessor != null) {
            getNoteRenderingPreProcessorManager()
                    .removeProcessor(watchedDiscussionRenderingPreProcessor);
        }
        WidgetExtensionManagementRepository extensionRepo = getWidgetExtensionManagementRepository();
        unregisterWidgetExtension(extensionRepo, watchDiscussionActionProvider);
        unregisterWidgetExtension(extensionRepo, watchDiscussionMetadataProvider);
    }

    private void unregisterDefinition(NotificationService notificationService,
            NotificationDefinition notificationDefinition) {
        if (notificationDefinition != null) {
            notificationService.unregister(notificationDefinition);
        }
    }

    private void unregisterManager(NoteStoringPostProcessorManager postProcessorManager,
            NotificationNoteProcessor notificationProcessor) {
        if (notificationProcessor != null) {
            postProcessorManager.removeProcessor(notificationProcessor);
        }
    }

    private void unregisterWidgetExtension(WidgetExtensionManagementRepository extensionRepo,
            WidgetExtension extension) {
        if (extension != null) {
            extensionRepo.removeExtension(extension);
        }
    }
}
