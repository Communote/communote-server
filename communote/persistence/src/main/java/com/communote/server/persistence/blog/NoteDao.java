package com.communote.server.persistence.blog;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;

/**
 * @see Note
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface NoteDao {

    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes no transformation will occur.
     */
    public final static int TRANSFORM_NONE = 0;

    /**
     * Creates a new instance of Note and adds from the passed in <code>entities</code> collection
     * 
     * @param entities
     *            the collection of Note instances to create.
     * 
     * @return the created instances.
     */
    public Collection<Note> create(Collection<Note> entities);

    /**
     * <p>
     * Does the same thing as {@link #create(Note)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entities (into value objects for example). By default,
     * transformation does not occur.
     * </p>
     */
    public Collection<?> create(int transform, Collection<Note> entities);

    /**
     * <p>
     * Does the same thing as {@link #create(Note)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entity (into a value object for example). By default, transformation
     * does not occur.
     * </p>
     */
    public Object create(int transform, Note note);

    /**
     * Creates an instance of Note and adds it to the persistent store.
     */
    public Note create(Note note);

    /**
     * Evicts (removes) the entity from the hibernate cache
     * 
     * @param entity
     *            the entity to evict
     */
    public void evict(Note entity);

    /**
     * <p>
     * Returns the note with highest ID.
     * </p>
     */
    public Note findLatestNote();

    /**
     * <p>
     * Returns the nearest note to the given Id.
     * </p>
     */
    public Note findNearestNote(long noteId, Date creationDate, boolean younger);

    /**
     * Load a note from the persistent store, even if it is cached. In case the note was cached the
     * updated note will be put into the cache.
     * 
     * Note: when called from within a session where the entity is already loaded, the returned
     * object will be the one previously loaded into the session. However, a normal load from a
     * later session will return the latest from the persistent storage.
     * 
     * @param id
     *            the ID of the note to load
     * @return the loaded note or null if there is no note with the given ID
     */
    public Note forceLoad(Long id);

    /**
     * <p>
     * Tries to find an autosaved note of the given user. If noteId is not null the returned
     * autosave was created during an edit operation of that note. If noteId is null and
     * parentNoteId is not null the returned autosave was created during an answer operation on that
     * note.
     * </p>
     * <p>
     * 
     * @return the ID of the note or null if not found
     *         </p>
     */
    public Long getAutosave(Long userId, Long noteId, Long parentNoteId,
            FilterNoteProperty[] properties);

    /**
     * <p>
     * Returns the favorite notes of the given user within the given range.
     * </p>
     */
    public Collection<Long> getFavoriteNoteIds(Long userId, Long lowerBound, Long upperBound);

    /**
     * <p>
     * Returns the IDs of the notes of the discussion. The result does not include the root note of
     * the discussion.
     * </p>
     */
    public List<Long> getNoteIdsOfDiscussion(Long discussionId);

    /**
 * 
     */
    public List<Note> getNotesByTag(Long tagId);

    /**
     * <p>
     * Gets the count of all notes
     * </p>
     */
    public long getNotesCount();

    /**
     * <p>
     * 
     * @return List of notes for the given blog and parameters.
     *         </p>
     */
    public List<Note> getNotesForBlog(Long blogId, Long firstNoteId, Integer limit);

    /**
 * 
     */
    public List<Note> getNotesOfUser(Long userId);

    /**
     * <p>
     * Returns how often a note is favorized.
     * </p>
     * 
     * @param noteId
     *            the note to check
     * @return the number of favorites
     */
    public int getNumberOfFavorites(Long noteId);

    /**
     * Test whether a discussion has notes which do not have the provided topic.
     * 
     * @param discussionId
     *            ID of the discussion
     * @param topicId
     *            the ID of the topic to test
     * @return true if there are notes that do not have the provided topic
     */
    public boolean hasInconsistentTopics(Long discussionId, Long topicId);

    /**
     * <p>
     * Does the same thing as {@link #load(Long)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined in this class then the result <strong>WILL BE</strong> passed through an operation
     * which can optionally transform the entity (into a value object for example). By default,
     * transformation does not occur.
     * </p>
     * 
     * @param id
     *            the identifier of the entity to load.
     * @return either the entity or the object transformed from the entity.
     */
    public Object load(int transform, Long id);

    /**
     * Loads an instance of Note from the persistent store.
     */
    public Note load(Long id);

    /**
     * Loads all entities of type {@link Note}.
     * 
     * @return the loaded entities.
     */
    public Collection<Note> loadAll();

    /**
     * <p>
     * Does the same thing as {@link #loadAll()} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entity (into a value object for example). By default, transformation
     * does not occur.
     * </p>
     * 
     * @param transform
     *            the flag indicating what transformation to use.
     * @return the loaded entities.
     */
    public Collection<?> loadAll(final int transform);

    /**
     * Moves the given discussion to the given topic.
     * 
     * @param discussionId
     *            The discussion to move.
     * @param newTopic
     *            The new topic of the discussion.
     */
    public void moveToTopic(Long discussionId, Blog newTopic);

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove(Collection<Note> entities);

    /**
     * Removes the instance of Note having the given <code>identifier</code> from the persistent
     * store.
     */
    public void remove(Long id);

    /**
     * Removes the instance of Note from the persistent store.
     */
    public void remove(Note note);

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update(Collection<Note> entities);

    /**
     * Updates the <code>note</code> instance in the persistent store.
     */
    public void update(Note note);

    /**
     * <p>
     * Updates the followableItems of a Note to the current settings. Should be called after the
     * note was updated/created.
     * </p>
     * 
     * @param updateChildren
     *            If true, childrens will be updated too.
     */
    public void updateFollowableItems(Note note, boolean updateChildren);

}
