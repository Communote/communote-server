package com.communote.server.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.converter.Converter;
import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.DiscussionChangedEvent;
import com.communote.server.core.blog.MovingOfNonRootNotesNotAllowedException;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.vo.blog.AutosaveNoteData;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.core.vo.uti.UserNotificationResult;
import com.communote.server.model.note.Note;
import com.communote.server.persistence.blog.FilterNoteProperty;
import com.communote.server.persistence.blog.NoteDao;

/**
 * The NoteService is the service implementation over the more database oriented
 * {@link NoteManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("noteService")
public class NoteService {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(NoteService.class);

    @Autowired
    private NoteManagement noteManagement;
    @Autowired
    private EventDispatcher eventDispatcher;
    @Autowired
    private PropertyManagement propertyManagement;

    /**
     * Creates a note from the supplied transfer object.
     *
     * @param noteStoringTO
     *            The note to create.
     * @param additionalBlogNameIds
     *            set of blog aliases for creating crossposts. These aliases will be ignore, if the
     *            note is a comment to another note.
     * @return The result of this operation.
     * @throws BlogNotFoundException
     *             in case the target blog does not exist
     * @throws NoteManagementAuthorizationException
     *             in case the user is not authorized to create the note, for instance if he has no
     *             write access to the target blog
     * @throws NoteStoringPreProcessorException
     *             in case one of the pre processors failed
     */
    public NoteModificationResult createNote(NoteStoringTO noteStoringTO,
            Set<String> additionalBlogNameIds) throws BlogNotFoundException,
            NoteManagementAuthorizationException, NoteStoringPreProcessorException {
        return createNote(noteStoringTO, additionalBlogNameIds, null);
    }

    /**
     * Creates a note from the supplied transfer object.
     *
     * @param noteStoringTO
     *            The note to create.
     * @param additionalBlogNameIds
     *            set of blog aliases for creating crossposts. These aliases will be ignore, if the
     *            note is a comment to another note.
     * @param autosaveFilterProperties
     *            Additional properties for finding the autosave.
     * @return The result of this operation.
     * @throws BlogNotFoundException
     *             in case the target blog does not exist
     * @throws NoteManagementAuthorizationException
     *             in case the user is not authorized to create the note, for instance if he has no
     *             write access to the target blog
     * @throws NoteStoringPreProcessorException
     *             in case one of the pre processors failed
     */
    public NoteModificationResult createNote(NoteStoringTO noteStoringTO,
            Set<String> additionalBlogNameIds, FilterNoteProperty[] autosaveFilterProperties)
            throws BlogNotFoundException, NoteManagementAuthorizationException,
            NoteStoringPreProcessorException {
        NoteModificationResult result = noteManagement.createNote(noteStoringTO,
                additionalBlogNameIds, autosaveFilterProperties);
        if (noteStoringTO.isPublish() && NoteModificationStatus.SUCCESS.equals(result.getStatus())) {
            if (noteStoringTO.getParentNoteId() == null || !noteStoringTO.isIsDirectMessage()) {
                noteManagement.updateLastDiscussionCreationDate(result.getNoteId(),
                        noteStoringTO.getCreationDate());
            }
            // in case of a reply fire a discussion changed event, and consolidate the topics of the
            // discussion in case there was a concurrent move
            try {
                Long discussionId = getDiscussionId(result.getNoteId());
                if (!discussionId.equals(result.getNoteId())) {
                    eventDispatcher.fire(new DiscussionChangedEvent(discussionId));
                    noteManagement.correctTopicOfComment(result.getNoteId());
                }
            } catch (NoteNotFoundException e) {
                LOGGER.error(
                        "Unexpected exception while getting discussion ID of newly created note.",
                        e);
            }
        }
        return result;
    }

    /**
     * Deletes an autosave identified by the passed ID. In case there is no autosaved note with this
     * ID nothing will happen.
     *
     * @param noteId
     *            Id of the autosave to delete.
     * @throws NoteManagementAuthorizationException
     *             Thrown, when the user is not allowed to access the note.
     */
    public void deleteAutosave(Long noteId) throws NoteManagementAuthorizationException {
        noteManagement.deleteAutosave(noteId);
    }

    /**
     * Deletes a user tagged post and all its comments.
     *
     * @param noteId
     *            Id of the note to delete.
     * @param deleteSystemPosts
     *            If true system notes will also be deleted.
     * @param clientManagerCanDelete
     *            If true notes will also be deleted, if the current user is a client manager.
     * @throws NoteManagementAuthorizationException
     *             Thrown, when the user is not allowed to access the note.
     */
    public void deleteNote(Long noteId, boolean deleteSystemPosts, boolean clientManagerCanDelete)
            throws NoteManagementAuthorizationException {
        Long discussionId;
        try {
            discussionId = getDiscussionId(noteId);
        } catch (NoteNotFoundException e) {
            // ignore;
            return;
        }
        noteManagement.deleteNote(noteId, deleteSystemPosts, clientManagerCanDelete);
        // inform listeners about changed discussion
        eventDispatcher.fire(new DiscussionChangedEvent(discussionId));
        if (!noteId.equals(discussionId)) {
            // a reply was removed so we have to update the creation date of the last note of the
            // discussion
            updateLastDiscussionCreationDateAfterNoteDeletion(discussionId, noteId);
        }
    }

    /**
     * Deletes all notes and drafts of a user. Comments on these posts are converted to normal
     * notes. If the current user is not allowed to delete these notes, i.e. he is not client
     * manager or the creator of the posts, a NoteManagementAuthorizationException is thrown.+
     *
     * @param userId
     *            Id of the user, the notes should be deleted for.
     * @throws AuthorizationException
     *             Thrown, when it is not allowed to delete the notes of this user.
     */
    public void deleteNotesOfUser(Long userId) throws AuthorizationException {
        Set<Long> dicussionIds = noteManagement.deleteNotesOfUser(userId);
        for (Long discussionId : dicussionIds) {
            eventDispatcher.fire(new DiscussionChangedEvent(discussionId));
            updateLastDiscussionCreationDateAfterNoteDeletion(discussionId, null);
        }
    }

    /**
     * Return an autosave of the current user. The parameters noteId and parentNoteId can be used to
     * retrieve an autosave for an edit operation or a comment.
     *
     * @param noteId
     *            the ID of a note to get the autosave of the edit operation of this note. Can be
     *            null.
     * @param parentNoteId
     *            the ID of a parent note to get the autosave of a comment to this note. Can be
     *            null.
     * @param propertyFilters
     *            filters for finding an autosave. Can be null.
     * @param locale
     *            the locale to use when filling localizable content of the note like tags
     * @return the autosave or null if there is none
     */
    public AutosaveNoteData getAutosave(Long noteId, Long parentNoteId,
            FilterNoteProperty[] propertyFilters, Locale locale) {
        return noteManagement.getAutosave(noteId, parentNoteId, propertyFilters, locale);
    }

    /**
     * Returns all the notes of a discussion excluding the root note. Notes the current user is not
     * allowed to read, i.e. direct messages that were not sent to that user, won't be included
     * either. It is assumed that the provided user has read access to the blog of the notes. The
     * returned comments are sorted by the creation date in ascending order.
     *
     * @param noteId
     *            the ID of the note whose discussion will be evaluated
     * @return the notes in the discussion
     * @throws NoteNotFoundException
     *             in case the note does not exist
     */
    public List<SimpleNoteListItem> getCommentsOfDiscussion(Long noteId)
            throws NoteNotFoundException {
        return noteManagement.getCommentsOfDiscussion(noteId);
    }

    /**
     * Returns the ID of the discussion the note is part of.
     *
     * @param noteId
     *            the ID of the note for which the discussion ID should be returned
     * @return the discussion ID
     * @throws NoteNotFoundException
     *             in case the note does not exist
     */
    public Long getDiscussionId(Long noteId) throws NoteNotFoundException {
        return noteManagement.getDiscussionId(noteId);
    }

    /**
     * @param noteId
     *            The note id.
     * @param renderContext
     *            The rendering context. If the render mode of the context is null, no
     *            NoteRenderingPreProcessors will be called.
     * @return The note as NoteData.
     * @throws NoteNotFoundException
     *             Thrown, when there is no note for this id.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access the note.
     */
    public NoteData getNote(long noteId, NoteRenderContext renderContext)
            throws NoteNotFoundException, AuthorizationException {
        return noteManagement.getNote(noteId, renderContext);
    }

    /**
     * Method to get a specific note.
     *
     * @param noteId
     *            The notes id.
     * @param converter
     *            The converter to convert the note.
     * @param <T>
     *            Type of the return value.
     * @return The note in the desired converter output or null, if none.
     */
    public <T> T getNote(Long noteId, Converter<Note, T> converter) {
        return noteManagement.getNote(noteId, converter);
    }

    /**
     * Returns the number of notes created within the current client.
     *
     * @return Number of notes.
     */
    public long getNoteCount() {
        return noteManagement.getNoteCount();
    }

    /**
     * Returns a note with its comments which are ordered according to the provided converter.
     *
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
    public DiscussionNoteData getNoteWithComments(Long noteId,
            QueryResultConverter<SimpleNoteListItem, DiscussionNoteData> converter)
            throws NoteNotFoundException, AuthorizationException {
        return noteManagement.getNoteWithComments(noteId, converter);
    }

    /**
     * Returns the number of notes in a discussion. Notes the current user is not allowed to read,
     * i.e. direct messages that were not sent to that user, are not included. It is assumed that
     * the provided user has read access to the blog.
     *
     * @param noteId
     *            the ID of the note whose discussion will be evaluated
     * @return the number of notes in the discussion
     * @throws NoteNotFoundException
     *             in case the note does not exist
     */
    public int getNumberOfNotesInDiscussion(Long noteId) throws NoteNotFoundException {
        return noteManagement.getNumberOfNotesInDiscussion(noteId);
    }

    /**
     * Returns the number of replies to a note. This includes replies on replies and also replies
     * that are direct messages which the current user might not be able to read.
     *
     * @param noteId
     *            the ID of the note
     * @return the number of replies
     * @throws NoteNotFoundException
     *             Thrown, when the note doesn't exist anymore.
     */
    public int getNumberOfReplies(Long noteId) throws NoteNotFoundException {
        return noteManagement.getNumberOfReplies(noteId);
    }

    /**
     * Initialization stuff
     */
    @PostConstruct
    private void init() {
        propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty,
                PropertyManagement.KEY_GROUP, NoteManagement.USER_NOTE_PROPERTY_KEY_LIKE);
    }

    /**
     * Move whole discussion if there are notes which do not have the correct topic.
     *
     * @param discussionId
     *            the ID of the discussion
     * @param blogId
     *            the ID of the expected topic
     * @throws NoteManagementAuthorizationException
     *             should not occur
     */
    private void moveIfInconsistent(Long discussionId, Long blogId)
            throws NoteManagementAuthorizationException {
        if (ServiceLocator.findService(NoteDao.class).hasInconsistentTopics(discussionId, blogId)) {
            noteManagement.moveToTopic(discussionId, blogId);
            noteManagement.updateFollowableItems(discussionId, true);
        }
    }

    /**
     * Method to check if a note exists.
     *
     * @param noteId
     *            Id of the note to ask for.
     * @return <code>True</code>, if the note exits.
     */
    public boolean noteExists(Long noteId) {
        return noteManagement.noteExists(noteId);
    }

    /**
     * Method to update the last discussion date after a note was deleted.
     *
     * @param discussionId
     *            The discussion id.
     * @param noteId
     *            Id of the deleted note. Might be null.
     */
    private void updateLastDiscussionCreationDateAfterNoteDeletion(Long discussionId, Long noteId) {
        try {
            IdentityConverter<Note> noteConverter = new IdentityConverter<Note>();
            Note discussionRoot = noteManagement.getNote(discussionId, noteConverter);
            if (discussionRoot == null) {
                LOGGER.warn("Can't update last discussion creation date of discussion with id {}. "
                        + "The discussion might be removed.", discussionId);
                return;
            }
            List<SimpleNoteListItem> comments = noteManagement
                    .getCommentsOfDiscussion(discussionId);
            Timestamp lastDiscussionCreationDate = discussionRoot.getCreationDate();
            for (SimpleNoteListItem comment : comments) {
                if (!comment.getId().equals(noteId)
                        && lastDiscussionCreationDate.before(comment.getCreationDate())) {
                    Note commentNote = noteManagement.getNote(comment.getId(), noteConverter);
                    if (commentNote != null && !commentNote.isDirect()) {
                        lastDiscussionCreationDate = commentNote.getCreationDate();
                    }
                }
            }
            noteManagement.updateLastDiscussionCreationDate(discussionId,
                    lastDiscussionCreationDate);
        } catch (NoteNotFoundException e) {
            LOGGER.warn("Can't update last discussion creation date of discussion with id {}. "
                    + "The discussion might be removed.", discussionId);
        }
    }

    /**
     * Updates an existing note with the data of the supplied transfer object.
     *
     * @param noteStoringTO
     *            The note to create.
     * @param noteId
     *            the ID of the note to update
     * @param additionalBlogNameIds
     *            set of blog aliases for creating crossposts. These aliases will be ignore, if the
     *            note is a comment to another note.
     * @param resendNotifications
     *            whether to send notifications to mentioned users
     * @return The result of this operation.
     * @throws BlogNotFoundException
     *             in case the target blog does not exist
     * @throws NoteNotFoundException
     *             in case the note to update does not exist
     * @throws NoteManagementAuthorizationException
     *             in case the user is not authorized to update the note, for instance if he has no
     *             write access to the blog or is not the author of the note
     * @throws NoteStoringPreProcessorException
     *             in case one of the pre processors failed
     * @throws MovingOfNonRootNotesNotAllowedException
     *             Thrown, when the user tries to move a note, which is not a parent root.
     */
    public NoteModificationResult updateNote(NoteStoringTO noteStoringTO, Long noteId,
            Set<String> additionalBlogNameIds, boolean resendNotifications)
            throws BlogNotFoundException, NoteNotFoundException,
            NoteManagementAuthorizationException, NoteStoringPreProcessorException,
            MovingOfNonRootNotesNotAllowedException {
        if (noteId != null && noteStoringTO.getBlogId() != null
                && noteStoringTO.getContent() == null) {
            // This has to be done in two separate transaction, because
            // noteDao.updateFollowableItems is working on cached objects
            noteManagement.moveToTopic(noteId, noteStoringTO.getBlogId());
            noteManagement.updateFollowableItems(noteId, true);
            // correct wrong topics in discussion which can result from concurrent comment or edit
            // operations
            moveIfInconsistent(noteId, noteStoringTO.getBlogId());
            NoteModificationResult result = new NoteModificationResult();
            result.setUserNotificationResult(new UserNotificationResult());
            result.setStatus(NoteModificationStatus.SUCCESS);
            result.setMessageKey("note.move-discussion.success");
            return result;
        }
        NoteModificationResult result = noteManagement.updateNote(noteStoringTO, noteId,
                additionalBlogNameIds, resendNotifications);
        if (noteStoringTO.isPublish() && result.getStatus().equals(NoteModificationStatus.SUCCESS)) {
            Note editedNote = getNote(result.getNoteId(), new IdentityConverter<Note>());
            Long discussionId = editedNote.getDiscussionId();
            if (discussionId.equals(result.getNoteId())) {
                moveIfInconsistent(discussionId, editedNote.getBlog().getId());
            } else {
                noteManagement.correctTopicOfComment(result.getNoteId());
            }
        }
        return result;
    }
}
