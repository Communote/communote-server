package com.communote.server.core.note.processor;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.processor.NoteMetadataRenderingPreProcessor;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.core.blog.helper.NoteHelper;
import com.communote.server.core.converter.user.UserToUserDataConverter;
import com.communote.server.core.security.SecurityHelper;

/**
 * Preprocessor for adding information about "Like" to a note.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class LikeNoteRenderingPreProcessor implements NoteMetadataRenderingPreProcessor {

    /**
     * Property holding the users linking the note as Collection of UserListItems
     */
    public static final String PROPERTY_LIKERS = "usersWhichLikeThisPost";

    /**
     * Property denoting whether the current user likes the note. The value is a Boolean.
     */
    public static final String PROPERTY_LIKED = "currentUserLikesNote";

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(LikeNoteRenderingPreProcessor.class);

    private PropertyManagement propertyManagement;

    private final UserToUserDataConverter<UserData> converter = new UserToUserDataConverter<UserData>(
            UserData.class, false, null);

    /**
     * @return {@link NoteMetadataRenderingPreProcessor#DEFAULT_ORDER}
     */
    @Override
    public int getOrder() {
        return NoteMetadataRenderingPreProcessor.DEFAULT_ORDER;
    }

    /**
     * @return the propertyManagement
     */
    public PropertyManagement getPropertyManagement() {
        if (propertyManagement == null) {
            propertyManagement = ServiceLocator.instance().getService(PropertyManagement.class);
        }
        return propertyManagement;
    }

    /**
     * Processes the note.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean process(NoteRenderContext noteRenderContext,
            NoteData noteListData) {
        Long noteId = noteListData.getId();
        try {

            Collection<UserData> likers = NoteHelper.getLikersOfNote(noteId, converter);

            noteListData.setProperty(PROPERTY_LIKERS, likers);
            // check if current user is in the list
            Long currentUserId = SecurityHelper.getCurrentUserId();
            boolean currentUserLikesNote = false;
            // public user or internalSystem can't like the note
            if (currentUserId != null) {
                for (UserData user : likers) {
                    if (user.getId().equals(currentUserId)) {
                        currentUserLikesNote = true;
                        break;
                    }
                }
            }
            noteListData.setProperty(PROPERTY_LIKED, currentUserLikesNote);
            return true;
        } catch (AuthorizationException e) {
            // TODO should we fail here?
            LOG.warn("Illegal access of user " + SecurityHelper.getCurrentUserAlias() + " to note "
                    + noteId);
        } catch (NotFoundException e) {
            LOG.warn("User note property with keyGroup " + PropertyManagement.KEY_GROUP
                    + " and key " + NoteManagement.USER_NOTE_PROPERTY_KEY_LIKE
                    + " not found for note "
                    + noteId);
        }
        return false;
    }

    /**
     *
     * {@inheritDoc}
     *
     * @return true if not a repost mode.
     */
    @Override
    public boolean supports(NoteRenderMode noteRenderMode) {
        if (NoteRenderMode.REPOST.equals(noteRenderMode)
                || NoteRenderMode.REPOST_PLAIN.equals(noteRenderMode)) {
            return false;
        }
        return true;
    }
}
