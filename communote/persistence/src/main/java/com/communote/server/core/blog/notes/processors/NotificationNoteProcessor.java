/**
 *
 */
package com.communote.server.core.blog.notes.processors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorContext;
import com.communote.server.core.messaging.NotificationDefinition;
import com.communote.server.core.messaging.NotificationScheduleTypes;
import com.communote.server.core.messaging.NotificationService;
import com.communote.server.core.messaging.definitions.MentionNotificationDefinition;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.blog.NoteDao;

/**
 * Base class for note post processors that inform users about created and edited notes.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class NotificationNoteProcessor implements NoteStoringPostProcessor {
    private NoteDao noteDao;
    private NotificationService notificationService;

    /**
     * @return the note DAO
     */
    protected NoteDao getNoteDao() {
        if (noteDao == null) {
            noteDao = ServiceLocator.findService(NoteDao.class);
        }
        return noteDao;
    }

    /**
     * @return The notification definition this processor works for. This is used for filtering
     *         users. Default is MentionNotificationDefinition.
     */
    public NotificationDefinition getNotificationDefinition() {
        return MentionNotificationDefinition.INSTANCE;
    }

    /**
     * @return the notificationDefinitionService
     */
    protected NotificationService getNotificationService() {
        if (notificationService == null) {
            notificationService = ServiceLocator.findService(NotificationService.class);
        }
        return notificationService;
    }

    /**
     * Retrieves the users to be notified. Implementors need not to be concerned about filtering the
     * user not to notify because this is done in the {@link #processAsynchronously(Long, NoteStoringPostProcessorContext)}
     * method. Whether users should be ignored if they do not have read access to the blog mainly
     * depends on the type of the notification processor and is thus implementation specific.
     *
     * @param note
     *            the note to notify about
     * @param context
     *            the context
     * @return the users to notify, can be null
     */
    protected abstract Collection<User> getUsersToNotify(Note note,
            NoteStoringPostProcessorContext context);

    /**
     * @return True, if the notes author should also be notified, else false. Default is false.
     */
    public boolean notifyAuthor() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processAsynchronously(Long noteId, NoteStoringPostProcessorContext context) {
        Note note = getNoteDao().load(noteId);
        if (note == null) {
            return;
        }
        Set<Long> userIdsToSkip = context.getUserIdsToSkip();
        Collection<User> usersToNotify = getUsersToNotify(note, context);
        Collection<User> usersToSendMessage = new HashSet<User>();
        if (usersToNotify != null) {
            for (User user : usersToNotify) {
                if (!userIdsToSkip.contains(user.getId())
                        && (user.hasStatus(UserStatus.ACTIVE))
                        && getNotificationService().userHasSchedule(user.getId(),
                                getNotificationDefinition(), NotificationScheduleTypes.IMMEDIATE)
                                && (!note.getUser().getId().equals(user.getId()) || notifyAuthor())) {
                    usersToSendMessage.add(user);
                    userIdsToSkip.add(user.getId());
                }
            }
            if (usersToSendMessage.size() > 0) {
                getNotificationService().sendMessage(note, usersToSendMessage,
                        getNotificationDefinition());
            }
        }
    }

    /**
     * @param noteDao
     *            The note dao.
     */
    public void setNoteDao(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    /**
     * @param notificationService
     *            the notificationService to set
     */
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
