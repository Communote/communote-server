package com.communote.server.core.blog.notes.processors;

import java.util.Collection;
import java.util.Map;

import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorContext;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;

/**
 * Notifies users who were explicitly added to the note.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserNotificationNoteProcessor extends NotificationNoteProcessor {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return 100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<User> getUsersToNotify(Note note, NoteStoringPostProcessorContext context) {
        return note.getUsersToBeNotified();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean notifyAuthor() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(Note note, NoteStoringTO noteStoringTO, Map<String, String> properties) {
        boolean processMe = noteStoringTO.isSendNotifications()
                && note.getUsersToBeNotified() != null
                && !note.getUsersToBeNotified().isEmpty();

        return processMe;
    }
}
