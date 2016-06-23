package com.communote.server.persistence.global;

/**
 * @see com.communote.server.model.global.GlobalId
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface GlobalIdDao {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes no transformation will occur.
     */
    public final static int TRANSFORM_NONE = 0;

    /**
     * Creates an instance of com.communote.server.persistence.global.GlobalId and adds it to the
     * persistent store.
     */
    public com.communote.server.model.global.GlobalId create(
            com.communote.server.model.global.GlobalId globalId);

    /**
     * <p>
     * Does the same thing as {@link #create(com.communote.server.model.global.GlobalId)} with an
     * additional flag called <code>transform</code>. If this flag is set to
     * <code>TRANSFORM_NONE</code> then the returned entity will <strong>NOT</strong> be
     * transformed. If this flag is any of the other constants defined here then the result
     * <strong>WILL BE</strong> passed through an operation which can optionally transform the
     * entity (into a value object for example). By default, transformation does not occur.
     * </p>
     */
    public Object create(int transform, com.communote.server.model.global.GlobalId globalId);

    /**
     * <p>
     * Does the same thing as {@link #create(com.communote.server.model.global.GlobalId)} with an
     * additional flag called <code>transform</code>. If this flag is set to
     * <code>TRANSFORM_NONE</code> then the returned entity will <strong>NOT</strong> be
     * transformed. If this flag is any of the other constants defined here then the result
     * <strong>WILL BE</strong> passed through an operation which can optionally transform the
     * entities (into value objects for example). By default, transformation does not occur.
     * </p>
     */
    public java.util.Collection<?> create(int transform,
            java.util.Collection<com.communote.server.model.global.GlobalId> entities);

    /**
     * Creates a new instance of com.communote.server.persistence.global.GlobalId and adds from the
     * passed in <code>entities</code> collection
     * 
     * @param entities
     *            the collection of com.communote.server.persistence.global.GlobalId instances to
     *            create.
     * 
     * @return the created instances.
     */
    public java.util.Collection<com.communote.server.model.global.GlobalId> create(
            java.util.Collection<com.communote.server.model.global.GlobalId> entities);

    /**
 * 
     */
    public com.communote.server.model.global.GlobalId createGlobalId(
            com.communote.server.model.attachment.Attachment attachment);

    /**
 * 
     */
    public com.communote.server.model.global.GlobalId createGlobalId(
            com.communote.server.model.blog.Blog blog);

    /**
 * 
     */
    public com.communote.server.model.global.GlobalId createGlobalId(
            com.communote.server.model.note.Note note);

    /**
 * 
     */
    public com.communote.server.model.global.GlobalId createGlobalId(
            com.communote.server.model.tag.Tag tag);

    /**
 * 
     */
    public com.communote.server.model.global.GlobalId createGlobalId(
            com.communote.server.model.user.group.Group group);

    /**
 * 
     */
    public com.communote.server.model.global.GlobalId createGlobalId(
            com.communote.server.model.user.User user);

    /**
     * Evicts (removes) the entity from the hibernate cache
     * 
     * @param entity
     *            the entity to evict
     */
    public void evict(com.communote.server.model.global.GlobalId entity);

    /**
     * <p>
     * Returns a global ID matching the global identifier or null if there is no matching global ID.
     * </p>
     */
    public com.communote.server.model.global.GlobalId findByGlobalIdentifier(String globalIdentifier);

    /**
     * <p>
     * Returns the latest GlobalID, which is the GlobalID with highest ID member. The return value
     * might be null if there is no global ID yet.
     * </p>
     */
    public com.communote.server.model.global.GlobalId findLatestGlobalId();

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
     * Loads an instance of com.communote.server.persistence.global.GlobalId from the persistent
     * store.
     */
    public com.communote.server.model.global.GlobalId load(Long id);

    /**
     * Loads all entities of type {@link com.communote.server.model.global.GlobalId}.
     * 
     * @return the loaded entities.
     */
    public java.util.Collection<com.communote.server.model.global.GlobalId> loadAll();

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
    public java.util.Collection<?> loadAll(final int transform);

    /**
     * Removes the instance of com.communote.server.persistence.global.GlobalId from the persistent
     * store.
     */
    public void remove(com.communote.server.model.global.GlobalId globalId);

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove(java.util.Collection<com.communote.server.model.global.GlobalId> entities);

    /**
     * Removes the instance of com.communote.server.persistence.global.GlobalId having the given
     * <code>identifier</code> from the persistent store.
     */
    public void remove(Long id);

    /**
     * Updates the <code>globalId</code> instance in the persistent store.
     */
    public void update(com.communote.server.model.global.GlobalId globalId);

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update(java.util.Collection<com.communote.server.model.global.GlobalId> entities);

}
