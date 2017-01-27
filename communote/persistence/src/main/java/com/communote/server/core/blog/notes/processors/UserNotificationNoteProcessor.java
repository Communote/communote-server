package com.communote.server.core.blog.notes.processors;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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

    @Override
    public String getId() {
        return "userMention";
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    protected Collection<User> getUsersToNotify(Note note, NoteStoringPostProcessorContext context,
            Set<Long> userIdsToSkip) {
        return note.getUsersToBeNotified();
    }

    @Override
    protected boolean isSendNotifications(Note note, NoteStoringTO noteStoringTO,
            Map<String, String> properties, NoteNotificationDetails resendDetails) {
        // TODO only process if not in already informed users?
        return !note.getUsersToBeNotified().isEmpty();
    }

    @Override
    public boolean notifyAuthor() {
        return true;
    }
}
