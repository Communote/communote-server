package com.communote.server.core.blog.notes.processors;

import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringImmutableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.PropertyHelper;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;

public class EditNotificationNoteStoringPreProcessor implements
NoteStoringImmutableContentPreProcessor {
    public static final String PROPERTY_KEY_RESEND_NOTIFICATION = "editNote.resendNotification";
    public static final String TRANSIENT_PROPERTY_KEY_RESEND_NOTIFICATION = PropertyManagement.KEY_GROUP
            + "." + PROPERTY_KEY_RESEND_NOTIFICATION;

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Override
    public boolean isProcessAutosave() {
        return false;
    }

    @Override
    public NoteStoringTO process(NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException {
        return noteStoringTO;
    }

    @Override
    public NoteStoringTO processEdit(Note noteToEdit, NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException {
        StringPropertyTO property = PropertyHelper.removePropertyTO(noteStoringTO.getProperties(),
                PropertyManagement.KEY_GROUP, PROPERTY_KEY_RESEND_NOTIFICATION);
        // TODO treat unset property as "don't resend notifications" (reply via other clients, like
        // email)?
        if (property != null && !Boolean.parseBoolean(property.getPropertyValue())) {
            // save currently notified users
            NoteNotificationDetails notificationDetails = new NoteNotificationDetails();
            notificationDetails
            .setMentionDiscussionAuthors(noteToEdit.isMentionDiscussionAuthors());
            notificationDetails.setMentionTopicAuthors(noteToEdit.isMentionTopicAuthors());
            notificationDetails.setMentionTopicManagers(noteToEdit.isMentionTopicManagers());
            notificationDetails.setMentionTopicReaders(noteToEdit.isMentionTopicReaders());
            for (User user : noteToEdit.getUsersToBeNotified()) {
                notificationDetails.addMentionedUser(user.getId());
            }
            noteStoringTO.setTransientProperty(TRANSIENT_PROPERTY_KEY_RESEND_NOTIFICATION,
                    notificationDetails);
        }
        return noteStoringTO;
    }
}
