package com.communote.server.core.blog;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.vo.blog.AutosaveNoteData;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.model.note.Note;

/**
 * Management class for notes.
 * <p>
 * <b>Note:</b> Usage of this class directly is prohibited. To avoid errors
 * {@link com.communote.server.service.NoteService} should be used.
 * </p>
 */
public interface NoteManagement {

    /** Pattern used for mention all discussion participants. */
    public static final String CONSTANT_MENTION_DISCUSSION_PARTICIPANTS = "@@discussion";
    /** Pattern used for mention all managers of the topic. */
    public static final String CONSTANT_MENTION_TOPIC_MANAGERS = "@@managers";
    /** Pattern used for mention all users with at least one note within the topic. */
    public static final String CONSTANT_MENTION_TOPIC_AUTHORS = "@@authors";
    /** Pattern used for mention all users with at least read access to the topic. */
    public static final String CONSTANT_MENTION_TOPIC_READERS = "@@all";
    /** Property key for the like feature. */
    public static final String USER_NOTE_PROPERTY_KEY_LIKE = "like";

    /**
     * Corrects the topic of a comment note if it does not equal the topic of the discussion root
     * note. This method helps to resolve inconsistent discussions if move and edit or comment
     * operations ran concurrently.
     *
     * @param noteId
     *            ID of the comment note that was created or updated
     */
    public void correctTopicOfComment(Long noteId);

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
            NoteManagementAuthorizationException, NoteStoringPreProcessorException;

    /**
     * Deletes an autosave identified by the passed ID. In case there is no autosaved note with this
     * ID nothing will happen.
     *
     * @param noteId
     *            The notes id.
     *
     * @throws NoteManagementAuthorizationException
     *             Thrown, when it is not possible to access the note.
     */
    public void deleteAutosave(Long noteId) throws NoteManagementAuthorizationException;

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
            throws NoteManagementAuthorizationException;

    /**
     * Delete all notes and drafts of a user. If one of these notes has comments it is not deleted,
     * the content is anonymized instead to preserve the discussion. Additional note data like
     * attachments, mentions and tags are removed.
     *
     * @param userId
     *            ID of the user whose notes should be deleted
     * @throws AuthorizationException
     *             in case the current user is not the internal system user, the client manager or
     *             the creator of the notes
     * @return the IDs of the discussions which were modified because of the removal of notes
     */
    public Set<Long> deleteNotesOfUser(Long userId) throws AuthorizationException;

    /***
     * Return an autosave of the current user. The parameters noteId and parentNoteId can be used to
     * retrieve an autosave for an edit operation or a comment.
     *
     * @param noteId
     *            the ID of a note to get the autosave of the edit operation of this note. Can be
     *            null.
     * @param parentNoteId
     *            the ID of a parent note to get the autosave of a comment to this note. Can be
     *            null.
     * @param properties
     *            properties which should be used to get filters for finding an autosave. The
     *            filters are resolved with the help of the registered
     *            {@link com.communote.server.api.core.note.AutosavePropertyFilterProvider} the re
     *            Can be null.
     * @param locale
     *            the locale to use when filling localizable content of the note like tags
     * @return the autosave or null if there is none
     */
    public AutosaveNoteData getAutosave(Long noteId, Long parentNoteId,
            Collection<StringPropertyTO> properties, Locale locale);

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
            throws NoteNotFoundException;

    /**
     * Returns the ID of the discussion the note is part of.
     *
     * @param noteId
     *            the ID of the note for which the discussion ID should be returned
     * @return the discussion ID
     * @throws NoteNotFoundException
     *             in case the note does not exist
     */
    public Long getDiscussionId(Long noteId) throws NoteNotFoundException;

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
            throws NoteNotFoundException, AuthorizationException;

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
    public <T> T getNote(Long noteId, Converter<Note, T> converter);

    /**
     * Returns the number of notes created within the current client.
     *
     * @return The actual count of notes.
     */
    public long getNoteCount();

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
            throws NoteNotFoundException,
            com.communote.server.api.core.security.AuthorizationException;

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
    public int getNumberOfNotesInDiscussion(Long noteId) throws NoteNotFoundException;

    /**
     * Returns the number of replies to a note. This includes replies on replies and also replies
     * that are direct messages which the current user might not be able to read.
     *
     * @param noteId
     *            the ID of the note
     * @return the number of replies
     * @throws NoteNotFoundException
     *             Thrown, when the note doesn't exist.
     */
    public int getNumberOfReplies(Long noteId) throws NoteNotFoundException;

    /**
     * Method to move a given discussion to another topic.
     *
     * @param discussionId
     *            Id of the discussion to move.
     * @param topicId
     *            If of the new topic.
     * @throws NoteManagementAuthorizationException
     *             Thrown, when the use isn't allowed to write within the given topic.
     */
    public void moveToTopic(Long discussionId, Long topicId)
            throws NoteManagementAuthorizationException;

    /**
     * Checks whether a note exists. It is not checked whether the current user has read access to
     * the note.
     *
     * @param noteId
     *            the ID of the note to retrieve
     * @return true if the note exists, false otherwise
     */
    public boolean noteExists(Long noteId);

    /**
     * Update the followable items of a given note and optionally although of its children.
     *
     * @param noteId
     *            Id of the note to update.
     * @param updateChildren
     *            If true, all children will be updated too.
     */
    public void updateFollowableItems(Long noteId, boolean updateChildren);

    /**
     * Method to update the last discussion creation date.
     *
     * @param noteId
     *            Id of the note, which is part of the discussion.
     * @param creationDate
     *            The new date to set.
     */
    public void updateLastDiscussionCreationDate(Long noteId, Timestamp creationDate);

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
     */
    public NoteModificationResult updateNote(NoteStoringTO noteStoringTO, Long noteId,
            java.util.Set<String> additionalBlogNameIds) throws BlogNotFoundException,
            NoteNotFoundException, NoteManagementAuthorizationException,
            NoteStoringPreProcessorException;

}
