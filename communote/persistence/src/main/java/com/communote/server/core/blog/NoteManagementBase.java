package com.communote.server.core.blog;

import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.persistence.blog.CreateBlogPostHelper;

/**
 * <p>
 * Spring Service base class for <code>NoteManagement</code>, provides access to all services and
 * entities referenced by this service.
 * </p>
 *
 * @see com.communote.server.service.NoteService
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class NoteManagementBase implements NoteManagement {

    @Override
    public NoteModificationResult createNote(NoteStoringTO noteStoringTO,
            Set<String> additionalBlogNameIds) throws BlogNotFoundException,
            NoteManagementAuthorizationException, NoteStoringPreProcessorException {
        if (noteStoringTO == null) {
            throw new IllegalArgumentException("NoteManagement.createNote("
                    + "NoteStoringTO noteStoringTO, java.util.Set<String> additionalBlogNameIds)"
                    + " - 'noteStoringTO' can not be null");
        }
        if (noteStoringTO.getCreatorId() == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.createNote(NoteStoringTO noteStoringTO, java.util.Set<String> additionalBlogNameIds) - 'noteStoringTO.creatorId' can not be null");
        }
        if (noteStoringTO.getBlogId() == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.createNote(NoteStoringTO noteStoringTO, java.util.Set<String> additionalBlogNameIds) - 'noteStoringTO.blogId' can not be null");
        }
        if (noteStoringTO.getCreationSource() == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.createNote(NoteStoringTO noteStoringTO, java.util.Set<String> additionalBlogNameIds) - 'noteStoringTO.creationSource' can not be null");
        }
        if (noteStoringTO.getContentType() == null) {
            noteStoringTO.setContentType(NoteContentType.UNKNOWN);
        }
        if (noteStoringTO.getFailDefinition() == null) {
            CreateBlogPostHelper.setDefaultFailLevel(noteStoringTO);
        }
        try {
            return this.handleCreateNote(noteStoringTO, additionalBlogNameIds);
        } catch (RuntimeException rt) {
            throw new NoteManagementException(
                    "Error performing 'NoteManagement.createNote(NoteStoringTO noteStoringTO, java.util.Set<String> additionalBlogNameIds)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAutosave(Long noteId) throws NoteManagementAuthorizationException {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.deleteAutosave(Long noteId) - 'noteId' can not be null");
        }
        try {
            this.handleDeleteAutosave(noteId);
        } catch (RuntimeException rt) {
            throw new NoteManagementException(
                    "Error performing 'NoteManagement.deleteAutosave(Long noteId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNote(Long noteId, boolean deleteSystemPosts, boolean clientManagerCanDelete)
            throws NoteManagementAuthorizationException {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.deleteNote(Long noteId, boolean deleteSystemPosts, boolean clientManagerCanDelete) - 'noteId' can not be null");
        }
        try {
            this.handleDeleteNote(noteId, deleteSystemPosts, clientManagerCanDelete);
        } catch (RuntimeException rt) {
            throw new NoteManagementException(
                    "Error performing 'NoteManagement.deleteNote(Long noteId, boolean deleteSystemPosts, boolean clientManagerCanDelete)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Long> deleteNotesOfUser(Long userId) throws AuthorizationException {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.deleteNotesOfUser(Long userId) - 'userId' can not be null");
        }
        try {
            return handleDeleteNotesOfUser(userId);
        } catch (RuntimeException rt) {
            throw new NoteManagementException(
                    "Error performing 'NoteManagement.deleteNotesOfUser(Long userId)' --> " + rt,
                    rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SimpleNoteListItem> getCommentsOfDiscussion(Long noteId)
            throws NoteNotFoundException {
        if (noteId == null) {
            throw new IllegalArgumentException("The noteId cannot be null");
        }
        return handleGetCommentsOfDiscussion(noteId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Long getDiscussionId(Long noteId)
            throws com.communote.server.core.blog.NoteNotFoundException {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.getNoteById(Long noteId) - 'noteId' can not be null");
        }
        try {
            return this.handleGetDiscussionId(noteId);
        } catch (RuntimeException rt) {
            throw new NoteManagementException(
                    "Error performing 'NoteManagement.getNoteById(Long noteId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public DiscussionNoteData getNoteWithComments(Long noteId,
            QueryResultConverter<SimpleNoteListItem, DiscussionNoteData> converter)
            throws com.communote.server.core.blog.NoteNotFoundException,
            com.communote.server.api.core.security.AuthorizationException {
        if (noteId == null) {
            throw new IllegalArgumentException("'noteId' can not be null");
        }
        try {
            return this.handleGetNoteWithComments(noteId, converter);
        } catch (RuntimeException rt) {
            throw new NoteManagementException(
                    "Error performing 'NoteManagement.getNoteWithCommentsById(noteId, converter)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfNotesInDiscussion(Long noteId) throws NoteNotFoundException {
        if (noteId == null) {
            throw new IllegalArgumentException("The noteId cannot be null");
        }
        return handleGetNumberOfNotesInDiscussion(noteId);
    }

    /**
     * @see com.communote.server.service.NoteService#getNumberOfReplies(Long)
     */
    @Override
    @Transactional(readOnly = true)
    public int getNumberOfReplies(Long noteId)
            throws com.communote.server.core.blog.NoteNotFoundException {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.getNumberOfReplies(Long noteId) - 'noteId' can not be null");
        }
        try {
            return this.handleGetNumberOfReplies(noteId);
        } catch (RuntimeException rt) {
            throw new NoteManagementException(
                    "Error performing 'NoteManagement.getNumberOfReplies(Long noteId)' --> " + rt,
                    rt);
        }
    }

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     *
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }

    /**
     * Performs the core logic for {@link #createNote(NoteStoringTO, java.util.Set<String>)}
     */
    protected abstract com.communote.server.core.vo.blog.NoteModificationResult handleCreateNote(
            com.communote.server.api.core.note.NoteStoringTO noteStoringTO,
            java.util.Set<String> additionalBlogNameIds)
            throws com.communote.server.api.core.blog.BlogNotFoundException,
            NoteManagementAuthorizationException,
            com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

    /**
     * Performs the core logic for {@link #deleteAutosave(Long)}
     */
    protected abstract void handleDeleteAutosave(Long noteId)
            throws NoteManagementAuthorizationException;

    /**
     * Performs the core logic for {@link #deleteNote(Long, boolean, boolean)}
     */
    protected abstract void handleDeleteNote(Long noteId, boolean deleteSystemPosts,
            boolean clientManagerCanDelete) throws NoteManagementAuthorizationException;

    /**
     * Performs the core logic for {@link #deleteNotesOfUser(Long)}
     */
    protected abstract Set<Long> handleDeleteNotesOfUser(Long userId)
            throws com.communote.server.api.core.security.AuthorizationException;

    /**
     * Performs the core logic for {@link #getCommentsOfDiscussion(Long)}
     *
     * @param noteId
     *            the ID of the note
     *
     * @return the notes
     * @throws NoteNotFoundException
     *             in case the note does not exists
     */
    protected abstract List<SimpleNoteListItem> handleGetCommentsOfDiscussion(Long noteId)
            throws NoteNotFoundException;

    /**
     * Implementation of {@link #getDiscussionId(Long)}
     *
     * @param noteId
     *            the ID of the note for which the discussion ID should be returned
     * @return the ID of the discussion
     * @throws com.communote.server.core.blog.NoteNotFoundException
     *             in case the note does not exist
     */
    protected abstract Long handleGetDiscussionId(Long noteId)
            throws com.communote.server.core.blog.NoteNotFoundException;

/**
     * Performs the core logic for
     * {@link #getNoteWithComments(Long,
     *  com.communote.server.core.vo.query.note.SimpleNoteListItemToDiscussionNoteDataConverter)
     * @param noteId
     *            the id of the note to retrieve
     * @param converter
     *            the converter to use for filling the comments
     * @return the object holding the data of the note
     * @throws NoteNotFoundException
     *             if the requested note does not exist
     * @throws AuthorizationException
     *             if the current user has no read access to the note
     */
    protected abstract DiscussionNoteData handleGetNoteWithComments(Long noteId,
            QueryResultConverter<SimpleNoteListItem, DiscussionNoteData> converter)
            throws com.communote.server.core.blog.NoteNotFoundException,
            com.communote.server.api.core.security.AuthorizationException;

    /**
     * Performs the core logic for {@link #getNumberOfNotesInDiscussion(Long)}
     *
     * @param noteId
     *            the ID of the note
     * @return the number notes
     * @throws NoteNotFoundException
     *             in case the note does not exists
     */
    protected abstract int handleGetNumberOfNotesInDiscussion(Long noteId)
            throws NoteNotFoundException;

    /**
     * Performs the core logic for {@link #getNumberOfReplies(Long)}
     */
    protected abstract int handleGetNumberOfReplies(Long noteId)
            throws com.communote.server.core.blog.NoteNotFoundException;

    /**
     * {@inheritDoc}
     */
    protected abstract com.communote.server.core.vo.blog.NoteModificationResult handleUpdateNote(
            com.communote.server.api.core.note.NoteStoringTO noteStoringTO, Long noteId,
            java.util.Set<String> additionalBlogNameIds, boolean resendNotifications)
            throws com.communote.server.core.blog.NoteNotFoundException,
            NoteManagementAuthorizationException,
            com.communote.server.api.core.note.processor.NoteStoringPreProcessorException,
            com.communote.server.api.core.blog.BlogNotFoundException;

    /**
     * {@inheritDoc}
     */
    @Override
    public com.communote.server.core.vo.blog.NoteModificationResult updateNote(
            com.communote.server.api.core.note.NoteStoringTO noteStoringTO, Long noteId,
            java.util.Set<String> additionalBlogNameIds, boolean resendNotifications)
            throws com.communote.server.core.blog.NoteNotFoundException,
            NoteManagementAuthorizationException,
            com.communote.server.api.core.note.processor.NoteStoringPreProcessorException,
            com.communote.server.api.core.blog.BlogNotFoundException {
        if (noteStoringTO == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.updateNote(NoteStoringTO noteStoringTO, Long noteId, java.util.Set<String> additionalBlogNameIds, boolean resendNotifications) - 'noteStoringTO' can not be null");
        }
        if (noteStoringTO.getCreatorId() == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.updateNote(NoteStoringTO noteStoringTO, Long noteId, java.util.Set<String> additionalBlogNameIds, boolean resendNotifications) - 'noteStoringTO.creatorId' can not be null");
        }
        if (noteStoringTO.getBlogId() == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.updateNote(NoteStoringTO noteStoringTO, Long noteId, java.util.Set<String> additionalBlogNameIds, boolean resendNotifications) - 'noteStoringTO.blogId' can not be null");
        }
        if (noteStoringTO.getCreationSource() == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.updateNote(NoteStoringTO noteStoringTO, Long noteId, java.util.Set<String> additionalBlogNameIds, boolean resendNotifications) - 'noteStoringTO.creationSource' can not be null");
        }
        if (noteStoringTO.getContent() == null || noteStoringTO.getContent().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "NoteManagement.updateNote(NoteStoringTO noteStoringTO, Long noteId, java.util.Set<String> additionalBlogNameIds, boolean resendNotifications) - 'noteStoringTO.content' can not be null or empty");
        }
        if (noteStoringTO.getContentType() == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.updateNote(NoteStoringTO noteStoringTO, Long noteId, java.util.Set<String> additionalBlogNameIds, boolean resendNotifications) - 'noteStoringTO.contentType' can not be null");
        }
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "NoteManagement.updateNote(NoteStoringTO noteStoringTO, Long noteId, java.util.Set<String> additionalBlogNameIds, boolean resendNotifications) - 'noteId' can not be null");
        }
        try {
            return this.handleUpdateNote(noteStoringTO, noteId, additionalBlogNameIds,
                    resendNotifications);
        } catch (RuntimeException rt) {
            throw new NoteManagementException(
                    "Error performing 'NoteManagement.updateNote(NoteStoringTO noteStoringTO, Long noteId, java.util.Set<String> additionalBlogNameIds, boolean resendNotifications)' --> "
                            + rt, rt);
        }
    }
}