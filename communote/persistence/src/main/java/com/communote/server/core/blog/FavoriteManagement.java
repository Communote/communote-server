package com.communote.server.core.blog;

import com.communote.server.api.core.security.AuthorizationException;

/**
 * Interface for favorite management.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface FavoriteManagement {

    /**
     * Compute the number of all favorites for the note. Can only be called by a system user.
     * 
     * @param noteId
     *            the note id
     * @return the number of favorites for the note
     */
    public int getNumberOfFavorites(Long noteId) throws AuthorizationException;

    /**
     * @param noteId
     *            The note.
     * @return True, when the given item is a favorite of the current user.
     */
    public boolean isFavorite(Long noteId);

    /**
     * 
     * @param noteId
     *            The notes id.
     * @throws NoteNotFoundException
     *             Exception.
     */
    public void markNoteAsFavorite(Long noteId)
            throws NoteNotFoundException;

    /**
     * 
     * @param noteId
     *            The notes id.
     * @throws NoteNotFoundException
     *             Exception.
     */
    public void unmarkNoteAsFavorite(Long noteId)
            throws NoteNotFoundException;
}
