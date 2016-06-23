package com.communote.server.persistence.tag;

import java.util.Collection;
import java.util.List;

import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.model.tag.Tag;

/**
 * @see Tag
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface TagDao {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes no transformation will occur.
     */
    public final static int TRANSFORM_NONE = 0;

    /**
     * Creates a new instance of Tag and adds from the passed in <code>entities</code> collection
     * 
     * @param entities
     *            the collection of Tag instances to create.
     * 
     * @return the created instances.
     */
    public Collection<Tag> create(Collection<Tag> entities);

    /**
     * Does the same thing as {@link #create(Tag)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entities (into value objects for example). By default,
     * transformation does not occur.
     */
    public Collection<Tag> create(int transform, Collection<Tag> entities);

    /**
     * Does the same thing as {@link #create(Tag)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entity (into a value object for example). By default, transformation
     * does not occur.
     */
    public Object create(int transform, Tag tag);

    /**
     * Creates an instance of Tag and adds it to the persistent store.
     */
    public Tag create(Tag tag);

    /**
     * Evicts (removes) the entity from the hibernate cache
     * 
     * @param entity
     *            the entity to evict
     */
    public void evict(Tag entity);

    /**
     * @return List of tags with the given prefix.
     */
    public List<TagData> findByPrefix(String prefix, ResultSpecification resultSpecification);

    /**
     * Finds a tag by its TagStore definition.
     */
    public Tag findByTagStore(String tagStoreTagId, String tagStoreAlias);

    /**
     * Return the IDs of all users that follow the tag with the given ID. The resulting list will be
     * empty if the tag does not exis.
     * 
     * @param tagId
     *            the ID of the tag for which the followers should be returned
     * @return the IDs of the followers
     */
    public List<Long> getFollowers(Long tagId);

    /**
     * Does the same thing as {@link #load(Long)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined in this class then the result <strong>WILL BE</strong> passed through an operation
     * which can optionally transform the entity (into a value object for example). By default,
     * transformation does not occur.
     * 
     * @param id
     *            the identifier of the entity to load.
     * @return either the entity or the object transformed from the entity.
     */
    public Object load(int transform, Long id);

    /**
     * Loads an instance of Tag from the persistent store.
     */
    public Tag load(Long id);

    /**
     * Loads all entities of type {@link Tag}.
     * 
     * @return the loaded entities.
     */
    public Collection<? extends Tag> loadAll();

    /**
     * Does the same thing as {@link #loadAll()} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entity (into a value object for example). By default, transformation
     * does not occur.
     * 
     * @param transform
     *            the flag indicating what transformation to use.
     * @return the loaded entities.
     */
    public Collection<? extends Tag> loadAll(final int transform);

    /**
     * Removes the instance of Tag having the given <code>identifier</code> from the persistent
     * store.
     */
    public void remove(Long id);

    /**
     * Removes the instance of Tag from the persistent store.
     */
    public void remove(Tag tag);

    /**
     * This method removes the given tag. The tag may only be a note tag.
     * 
     * @param oldTagId
     *            Id of the tag to delete.
     * @param newTagId
     *            Id of an optional new tag, the data of the old tag should be assigned to.
     */
    public void removeNoteTag(long oldTagId, Long newTagId);

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update(Collection<Tag> entities);

    /**
     * Updates the <code>tag</code> instance in the persistent store.
     */
    public void update(Tag tag);

}
