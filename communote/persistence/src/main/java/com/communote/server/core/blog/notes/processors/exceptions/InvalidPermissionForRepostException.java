package com.communote.server.core.blog.notes.processors.exceptions;

import com.communote.server.api.core.note.NoteManagementAuthorizationException;

/**
 * This exception is thrown, when a user tries to create a repost of a note she has no access to.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InvalidPermissionForRepostException extends NoteManagementAuthorizationException {
    private final Long userId;
    private final Long noteId;

    /**
     * Constructor.
     * 
     * @param userId
     *            User, which provoked the exception.
     * @param noteId
     *            Note, the user can't access.
     */
    public InvalidPermissionForRepostException(Long userId, Long noteId) {
        super("The user " + userId + "is not allowed to access note " + noteId, null);
        this.userId = userId;
        this.noteId = noteId;
    }

    /**
     * @return Note, the user can't access.
     */
    public Long getNoteId() {
        return noteId;
    }

    /**
     * @return User, which provoked the exception.urn
     */
    public Long getUserId() {
        return userId;
    }
}
