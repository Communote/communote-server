package com.communote.plugins.discussionnotification.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.discussionnotification.DiscussionNotificationActivator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.processor.NoteMetadataRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.messaging.NotificationDefinition;
import com.communote.server.core.messaging.NotificationScheduleTypes;
import com.communote.server.core.messaging.NotificationService;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.service.NoteService;

/**
 * Note rendering pre-processor which exposes a property to the note data object if the current user
 * is watching a discussion. This property will only be added if the note is the root note of the
 * discussion and the user has at least one of the discussion notifications enabled.
 *
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 */
public class WatchedDiscussionRenderingPreProcessor implements NoteMetadataRenderingPreProcessor {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(WatchedDiscussionRenderingPreProcessor.class);

    public static final String PROPERTY_KEY_CURRENT_USER_IS_WATCHING = DiscussionNotificationActivator.KEY_GROUP
            + ".currentUserWatches";

    private final NoteService noteService;
    private final NotificationService notificationService;
    private final PropertyManagement propertyManagement;
    private final NotificationDefinition watchedNotificationDefinition;
    private final NotificationDefinition participationNotificationDefinition;

    public WatchedDiscussionRenderingPreProcessor(NoteService noteService,
            NotificationService notificationService, PropertyManagement propertyManagement,
            NotificationDefinition watchedNotificationDefinition,
            NotificationDefinition participationNotificationDefinition) {
        this.noteService = noteService;
        this.notificationService = notificationService;
        this.propertyManagement = propertyManagement;
        this.watchedNotificationDefinition = watchedNotificationDefinition;
        this.participationNotificationDefinition = participationNotificationDefinition;
    }

    private boolean exposeProperty(Long currentUserId, NoteData note) {
        // only expose if note is root note of discussion and there is currently a real logged in
        // user
        if (note.getDiscussionId() != null && note.getDiscussionId().equals(note.getId())
                && currentUserId != null && !SecurityHelper.isInternalSystem()
                && !SecurityHelper.isPublicUser()) {
            return true;
        }
        return false;
    }

    @Override
    public int getOrder() {
        return NoteMetadataRenderingPreProcessor.DEFAULT_ORDER;
    }

    private boolean isWatching(Long currentUserId, NoteData note, String watchPropertyValue,
            boolean participationNotificationDisabled) throws NoteNotFoundException {
        if (Boolean.FALSE.toString().equals(watchPropertyValue)) {
            // explicitly disabled by user
            return false;
        }
        if (Boolean.TRUE.toString().equals(watchPropertyValue)) {
            // the user is watching the discussion because it was explicitly enabled, it doesn't
            // matter which notification is active
            return true;
        }
        // property not set (or unexpected value): schedule decides whether user is watching. If
        // participationSchedule is active user is watching if he participated in the discussion.
        if (!participationNotificationDisabled) {
            // shortcut if author of root note
            return note.getUser().getId().equals(currentUserId)
                    || noteService.isAuthorOfDiscussion(currentUserId, note.getDiscussionId());
        }
        // watched notification is enabled but this is an opt-in feature, thus user is not watching
        return false;
    }

    @Override
    public boolean process(NoteRenderContext context, NoteData note)
            throws NoteRenderingPreProcessorException {
        Long currentUserId = SecurityHelper.getCurrentUserId();
        if (exposeProperty(currentUserId, note)) {
            try {
                // skip if current user disabled the notification definitions for watched
                // discussions and those participated in
                boolean watchedNotificationDisabled = notificationService.userHasSchedule(
                        currentUserId, watchedNotificationDefinition,
                        NotificationScheduleTypes.NEVER);
                boolean participationNotificationDisabled = notificationService.userHasSchedule(
                        currentUserId, participationNotificationDefinition,
                        NotificationScheduleTypes.NEVER);
                if (watchedNotificationDisabled && participationNotificationDisabled) {
                    return false;
                }
                String watchPropertyValue = propertyManagement.getUserNotePropertyValue(
                        note.getId(), DiscussionNotificationActivator.KEY_GROUP,
                        DiscussionNotificationActivator.PROPERTY_KEY_WATCHED_DISCUSSION);
                note.setProperty(PROPERTY_KEY_CURRENT_USER_IS_WATCHING, isWatching(currentUserId,
                        note, watchPropertyValue, participationNotificationDisabled));
            } catch (NotFoundException | AuthorizationException e) {
                // can happen in rare cases when user or note is not found due to parallel deletion,
                // or current user has no access to the note anymore. Nothing we can do about it.
                LOGGER.debug("Determining watched state of discussion failed: {}", e.getMessage());
            }
        }
        return false;
    }

    @Override
    public boolean supports(NoteRenderMode mode) {
        // no need to expose the date when rendering a note for being reposted
        if (NoteRenderMode.REPOST.equals(mode) || NoteRenderMode.REPOST_PLAIN.equals(mode)) {
            return false;
        }
        return true;
    }

}
