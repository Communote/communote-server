package com.communote.server.persistence.resource;

import java.util.Collection;
import java.util.Date;

import com.communote.server.model.attachment.Attachment;

/**
 * @see Attachment
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface AttachmentDao {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes no transformation will occur.
     */
    public final static int TRANSFORM_NONE = 0;

    /**
     * Creates an instance of Attachment and adds it to the persistent store.
     */
    public Attachment create(Attachment attachment);

    /**
     * Creates a new instance of Attachment and adds from the passed in <code>entities</code>
     * collection
     * 
     * @param entities
     *            the collection of Attachment instances to create.
     * 
     * @return the created instances.
     */
    public Collection<Attachment> create(Collection<Attachment> entities);

    /**
     * <p>
     * Does the same thing as {@link #create(Attachment)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entity (into a value object for example). By default, transformation
     * does not occur.
     * </p>
     */
    public Object create(int transform, Attachment attachment);

    /**
     * <p>
     * Does the same thing as {@link #create(Attachment)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entities (into value objects for example). By default,
     * transformation does not occur.
     * </p>
     */
    public Collection<?> create(int transform, Collection<Attachment> entities);

    /**
     * Evicts (removes) the entity from the hibernate cache
     * 
     * @param entity
     *            the entity to evict
     */
    public void evict(Attachment entity);

    /**
     * <p>
     * Does the same thing as {@link #find(String, String)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then finder
     * results will <strong>NOT</strong> be transformed during retrieval. If this flag is any of the
     * other constants defined here then finder results <strong>WILL BE</strong> passed through an
     * operation which can optionally transform the entities (into value objects for example). By
     * default, transformation does not occur.
     * </p>
     */
    public Object find(int transform, String contentIdentifier, String repositoryIdentifier);

    /**
     * Find a resource by the given identifiers
     */
    public Attachment find(String contentIdentifier, String repositoryIdentifier);

    /**
     * Find an attachments with an empty content type to be migrated
     */
    public Attachment findContentTypeNull();

    /**
     * Finds the note by a given content id of an attachment.
     */
    public com.communote.server.model.note.Note findNoteByContentId(
            com.communote.server.core.crc.vo.ContentId contentId);

    /**
     * Find all attachments which are not connected with a note and which are older than the given
     * date.
     * 
     * @param upperUploadDate
     *            Attachments must be older than this date.
     * @return the IDs of the found attachments
     */
    public Collection<Long> findOrphanedAttachments(Date upperUploadDate);

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
     * Loads an instance of Attachment from the persistent store.
     */
    public Attachment load(Long id);

    /**
     * Loads all entities of type {@link Attachment}.
     * 
     * @return the loaded entities.
     */
    public Collection<Attachment> loadAll();

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
     * Removes the instance of Attachment from the persistent store.
     */
    public void remove(Attachment attachment);

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove(Collection<Attachment> entities);

    /**
     * Removes the instance of Attachment having the given <code>identifier</code> from the
     * persistent store.
     */
    public void remove(Long id);

    /**
     * Updates the <code>attachment</code> instance in the persistent store.
     */
    public void update(Attachment attachment);

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update(Collection<Attachment> entities);

}
