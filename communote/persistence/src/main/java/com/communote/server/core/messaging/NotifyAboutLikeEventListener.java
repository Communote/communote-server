package com.communote.server.core.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.api.core.property.PropertyEvent;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.PropertyEvent.PropertyEventType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.core.blog.helper.NoteHelper;
import com.communote.server.core.messaging.definitions.LikeNotificationDefinition;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NotifyAboutLikeEventListener implements EventListener<PropertyEvent> {

    private final NotificationService notificationService;
    private final NoteManagement noteManagement;
    private final UserManagement userManagement;
    private final PropertyManagement propertyManagement;

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(NotifyAboutLikeEventListener.class);

    private final Converter<User, Long> userToUserIdConverter = new Converter<User, Long>() {
        @Override
        public Long convert(User source) {
            return source.getId();
        }
    };

    /**
     * Constructor.
     * 
     * @param notificationService
     *            Service used for checking, if the user wants the notification.
     * @param noteManagement
     *            Used to retrieve additional note data.
     * @param userManagement
     *            Used to retrieve user data.
     * @param propertyManagement
     *            Used to retrieve property data.
     */
    public NotifyAboutLikeEventListener(
            NotificationService notificationService, NoteManagement noteManagement,
            UserManagement userManagement, PropertyManagement propertyManagement) {
        this.notificationService = notificationService;
        this.noteManagement = noteManagement;
        this.userManagement = userManagement;
        this.propertyManagement = propertyManagement;
    }

    /**
     * @return ProepertyEvent
     */
    @Override
    public Class<PropertyEvent> getObservedEvent() {
        return PropertyEvent.class;
    }

    @Override
    public void handle(PropertyEvent event) {
        try {
            if (!isLikeProperty(event)) {
                return;
            }
            Long noteId = event.getObjectId();
            Long author = noteManagement.getNote(noteId, new Converter<Note, Long>() {
                @Override
                public Long convert(Note source) {
                    return source.getUser().getId();
                }
            });
            if (!notificationService.userHasSchedule(author,
                    LikeNotificationDefinition.INSTANCE, NotificationScheduleTypes.IMMEDIATE)) {
                return;
            }
            Map<String, Object> model = new HashMap<String, Object>();
            String likingUserName = userManagement.getUserById(event.getUserId(),
                    new Converter<User, String>() {
                        @Override
                        public String convert(User source) {
                            return UserNameHelper.getSimpleDefaultUserSignature(source);
                        }
                    });
            model.put("likingUserName", likingUserName);

            Collection<Long> additionalLikesUserIds = NoteHelper.getLikersOfNote(noteId,
                    userToUserIdConverter);

            model.put("numberOfAdditionalLikes", additionalLikesUserIds.size() - 1);
            notificationService.sendMessage(noteId, author,
                    LikeNotificationDefinition.INSTANCE, model);
        } catch (NotFoundException e) {
            LOGGER.warn(e.getMessage());
        } catch (AuthorizationException e) {
            LOGGER.warn(e.getMessage());
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * @param event
     *            The event to check.
     * @return True, only on the first creation and if an "like" event.
     */
    private boolean isLikeProperty(PropertyEvent event) {
        return PropertyEventType.CREATE.equals(event.getPropertyEventType())
                && PropertyType.UserNoteProperty.equals(event.getPropertyType())
                && PropertyManagement.KEY_GROUP.equals(event.getKeyGroup())
                && NoteManagement.USER_NOTE_PROPERTY_KEY_LIKE.equals(event.getKey());
    }
}
