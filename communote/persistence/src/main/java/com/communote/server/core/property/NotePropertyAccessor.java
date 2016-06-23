package com.communote.server.core.property;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteProperty;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NotePropertyAccessor extends ObjectPropertyAccessor<Note, NoteProperty> {

    /**
     * Don't construct it from the outside packages.
     *
     * @param eventDispatcher
     *            the event dispatcher to use for firing events
     */
    protected NotePropertyAccessor(EventDispatcher eventDispatcher) {
        super(eventDispatcher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void assertReadAccess(Note note) throws AuthorizationException {
        // takes care of internalSystem and public user
        if (!ServiceLocator.findService(BlogRightsManagement.class)
                .currentUserHasReadAccess(note.getBlog().getId(), false)) {
            throw new AuthorizationException("The current user has no access to the topic "
                    + note.getBlog().getId());
        }
        // special checks required if the note is a DM
        if (note.isDirect()) {
            Long currentUserId = SecurityHelper.getCurrentUserId();
            if (currentUserId == null) {
                if (SecurityHelper.isInternalSystem()) {
                    return;
                }
                throw new AuthorizationException(
                        "Anonymous or public user has no access to direct messages");
            }
            // fail if current user isn't author or among the notified users
            if (!note.getUser().getId().equals(currentUserId)) {
                for (User notifiedUser : note.getUsersToBeNotified()) {
                    if (notifiedUser.getId().equals(currentUserId)) {
                        return;
                    }
                }
                throw new AuthorizationException(
                        "The current user (" + currentUserId
                                + ") does not have access to the given direct message ("
                                + note.getId() + ").");
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Checks, that the current user is the notes author.
     * </p>
     */
    @Override
    protected void assertWriteAccess(Note note) throws AuthorizationException {
        // handles public user
        if (!ServiceLocator.findService(BlogRightsManagement.class)
                .currentUserHasReadAccess(note.getBlog().getId(), false)) {
            throw new AuthorizationException(
                    "The current user is not allowed to write to the topic of the note");
        }
        Long currentUserId = SecurityHelper.getCurrentUserId();
        if (currentUserId == null && SecurityHelper.isInternalSystem()) {
            return;
        }
        if (!note.getUser().getId().equals(currentUserId)) {
            throw new AuthorizationException(
                    "The current user (" + currentUserId + ") has to be the author of the note ("
                            + note.getId() + ").");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long getObjectId(Note object) {
        return object.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyType getPropertyType() {
        return PropertyType.NoteProperty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NoteProperty handleCreateNewProperty(Note note) {
        NoteProperty property = NoteProperty.Factory.newInstance();
        note.getProperties().add(property);
        return property;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Note load(Long id) {
        return ServiceLocator.findService(NoteDao.class).load(id);
    }
}