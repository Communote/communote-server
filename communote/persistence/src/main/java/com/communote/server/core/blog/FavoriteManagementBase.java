package com.communote.server.core.blog;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.security.AuthorizationException;

/**
 * <p>
 * Spring Service base class for <code>com.communote.server.service.blog.FavoriteManagement</code>,
 * provides access to all services and entities referenced by this service.
 * </p>
 * 
 * @see com.communote.server.core.blog.FavoriteManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class FavoriteManagementBase
        implements com.communote.server.core.blog.FavoriteManagement {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfFavorites(Long noteId) throws AuthorizationException {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.blog.FavoriteManagement.getNumberOfFavorites"
                            + "(Long noteId) - 'noteId' can not be null");
        }
        try {
            return this.handleGetNumberOfFavorites(noteId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.blog.FavoriteManagementException(
                    "Error performing 'com.communote.server.service.blog.FavoriteManagement"
                            + ".getNumberOfFavorites(Long noteId)' --> " + rt, rt);
        }
    }

    /**
     * @see FavoriteManagementBase#getNumberOfFavorites(Long)
     */
    protected abstract int handleGetNumberOfFavorites(Long noteId) throws AuthorizationException;

    /**
     * Performs the core logic for {@link #markNoteAsFavorite(Long)}
     * 
     * @param noteId
     *            The notes id.
     * @throws NoteNotFoundException
     *             Exception.
     */
    protected abstract void handleMarkNoteAsFavorite(Long noteId)
            throws NoteNotFoundException;

    /**
     * Performs the core logic for {@link #unmarkNoteAsFavorite(Long)}
     * 
     * @param noteId
     *            The notes id.
     * @throws NoteNotFoundException
     *             Exception.
     */
    protected abstract void handleUnmarkNoteAsFavorite(Long noteId)
            throws NoteNotFoundException;

    /**
     * @see com.communote.server.core.blog.FavoriteManagement#markNoteAsFavorite(Long)
     * 
     * @param noteId
     *            The notes id.
     * @throws NoteNotFoundException
     *             Exception.
     */
    @Override
    public void markNoteAsFavorite(Long noteId)
            throws NoteNotFoundException {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.blog.FavoriteManagement.markNoteAsFavorite"
                            + "(Long noteId) - 'noteId' can not be null");
        }
        try {
            this.handleMarkNoteAsFavorite(noteId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.blog.FavoriteManagementException(
                    "Error performing 'com.communote.server.service.blog.FavoriteManagement"
                            + ".markNoteAsFavorite(Long noteId)' --> " + rt, rt);
        }
    }

    /**
     * @see com.communote.server.core.blog.FavoriteManagement#unmarkNoteAsFavorite(Long)
     * 
     *      {@inheritDoc}
     */
    @Override
    public void unmarkNoteAsFavorite(Long noteId)
            throws com.communote.server.core.blog.NoteNotFoundException {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.blog.FavoriteManagement.unmarkNoteAsFavorite"
                            + "(Long noteId) - 'noteId' can not be null");
        }
        try {
            this.handleUnmarkNoteAsFavorite(noteId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.blog.FavoriteManagementException(
                    "Error performing 'com.communote.server.service.blog.FavoriteManagement."
                            + "unmarkNoteAsFavorite(Long noteId)' --> " + rt, rt);
        }
    }
}