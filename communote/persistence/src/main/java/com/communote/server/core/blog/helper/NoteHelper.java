package com.communote.server.core.blog.helper;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;

import com.communote.common.converter.Converter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.model.note.Note;
import com.communote.server.model.property.StringProperty;
import com.communote.server.model.user.User;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * Helper class for notes.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public final class NoteHelper {

    public static <T extends Object> Collection<T> getLikersOfNote(Long noteId,
            Converter<User, T> converter) throws NotFoundException,
            AuthorizationException {
        Collection<T> likers = ServiceLocator.findService(PropertyManagement.class)
                .getUsersOfProperty(noteId,
                        PropertyManagement.KEY_GROUP,
                        NoteManagement.USER_NOTE_PROPERTY_KEY_LIKE,
                        Boolean.TRUE.toString(), converter);
        return likers;
    }

    /**
     * Get the note title for the note (e.g. "Max Mustermann (max) um 18:08)
     * 
     * @param note
     *            the note
     * @param locale
     *            the locale
     * @return title for the note
     */
    public static String getNoteTitle(Note note, Locale locale) {
        DateFormat noteDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
                locale);
        TimeZone effectiveUserTimeZone = UserManagementHelper.getEffectiveUserTimeZone();
        noteDate.setTimeZone(effectiveUserTimeZone);

        String noteTitle = ResourceBundleManager.instance().getText(
                "blog.post.single.item.title", locale,
                UserNameHelper.getDetailedUserSignature(note.getUser().getProfile()
                        .getFirstName(), note.getUser().getProfile().getLastName(), note
                        .getUser().getAlias()), noteDate.format(note.getCreationDate())
                );

        return noteTitle;
    }

    public static void likeNote(Long noteId) throws NotFoundException, AuthorizationException {
        ServiceLocator.findService(PropertyManagement.class).setObjectProperty(
                PropertyType.UserNoteProperty,
                noteId,
                PropertyManagement.KEY_GROUP,
                NoteManagement.USER_NOTE_PROPERTY_KEY_LIKE,
                Boolean.TRUE.toString());
    }

    public static boolean likesNote(Long noteId) throws NotFoundException, AuthorizationException {
        StringProperty prop = ServiceLocator.findService(PropertyManagement.class)
                .getObjectProperty(
                        PropertyType.UserNoteProperty,
                        noteId,
                        PropertyManagement.KEY_GROUP,
                        NoteManagement.USER_NOTE_PROPERTY_KEY_LIKE);
        if (prop == null || prop.getPropertyValue() == null) {
            return false;
        }
        return Boolean.parseBoolean(prop.getPropertyValue());
    }

    public static void setLikeNote(Long noteId, boolean value) throws NotFoundException,
            AuthorizationException {
        if (value) {
            likeNote(noteId);
        } else {
            unlikeNote(noteId);
        }
    }

    public static void unlikeNote(Long noteId) throws NotFoundException, AuthorizationException {
        ServiceLocator.findService(PropertyManagement.class).setObjectProperty(
                PropertyType.UserNoteProperty,
                noteId,
                PropertyManagement.KEY_GROUP,
                NoteManagement.USER_NOTE_PROPERTY_KEY_LIKE,
                Boolean.FALSE.toString());
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private NoteHelper() {
        // Do nothing
    }
}
