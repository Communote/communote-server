package com.communote.server.persistence.user.group;

import com.communote.server.model.user.group.ExternalUserGroup;

/**
 * @see com.communote.server.model.user.group.ExternalUserGroup
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ExternalUserGroupDao {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes no transformation will occur.
     */
    public final static int TRANSFORM_NONE = 0;

    /**
     * Creates an instance of com.communote.server.persistence.user.group.ExternalUserGroup and adds
     * it to the persistent store.
     */
    public com.communote.server.model.user.group.ExternalUserGroup create(
            com.communote.server.model.user.group.ExternalUserGroup externalUserGroup);

    /**
     * <p>
     * Does the same thing as
     * {@link #create(com.communote.server.model.user.group.ExternalUserGroup)} with an additional
     * flag called <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then
     * the returned entity will <strong>NOT</strong> be transformed. If this flag is any of the
     * other constants defined here then the result <strong>WILL BE</strong> passed through an
     * operation which can optionally transform the entity (into a value object for example). By
     * default, transformation does not occur.
     * </p>
     */
    public Object create(int transform,
            com.communote.server.model.user.group.ExternalUserGroup externalUserGroup);

    /**
     * <p>
     * Does the same thing as
     * {@link #create(com.communote.server.model.user.group.ExternalUserGroup)} with an additional
     * flag called <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then
     * the returned entity will <strong>NOT</strong> be transformed. If this flag is any of the
     * other constants defined here then the result <strong>WILL BE</strong> passed through an
     * operation which can optionally transform the entities (into value objects for example). By
     * default, transformation does not occur.
     * </p>
     */
    public java.util.Collection<?> create(int transform,
            java.util.Collection<com.communote.server.model.user.group.ExternalUserGroup> entities);

    /**
     * Creates a new instance of com.communote.server.persistence.user.group.ExternalUserGroup and
     * adds from the passed in <code>entities</code> collection
     * 
     * @param entities
     *            the collection of com.communote.server.persistence.user.group.ExternalUserGroup
     *            instances to create.
     * 
     * @return the created instances.
     */
    public java.util.Collection<com.communote.server.model.user.group.ExternalUserGroup> create(
            java.util.Collection<com.communote.server.model.user.group.ExternalUserGroup> entities);

    /**
     * Evicts (removes) the entity from the hibernate cache
     * 
     * @param entity
     *            the entity to evict
     */
    public void evict(com.communote.server.model.user.group.ExternalUserGroup entity);

    /**
     * Get an external group by the additional property. If more than one with the same property
     * exists the first found will be returned.
     * 
     * @param additionalProperty
     *            the additional property
     * @param externalSystemId
     *            the ID of the external system
     * @return the found external group or null
     */
    ExternalUserGroup findByAdditionalProperty(String additionalProperty, String externalSystemId);

    /**
 * 
     */
    public com.communote.server.model.user.group.ExternalUserGroup findByExternalId(
            String externalId, String externalSystemId);

    /**
 * 
     */
    public java.util.List<ExternalUserGroup> findBySystemId(String systemId);

    /**
     * <p>
     * Find the n newest external groups for the given external system ID. Newest in this context
     * means that the ID of the group is higher than the startId.
     * </p>
     */
    public java.util.List<ExternalUserGroup> findLatestBySystemId(String externalSystemId,
            Long startId, int maxCount);

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
     * Loads an instance of com.communote.server.persistence.user.group.ExternalUserGroup from the
     * persistent store.
     */
    public com.communote.server.model.user.group.ExternalUserGroup load(Long id);

    /**
     * Loads all entities of type {@link com.communote.server.model.user.group.ExternalUserGroup}.
     * 
     * @return the loaded entities.
     */
    public java.util.Collection<com.communote.server.model.user.group.ExternalUserGroup> loadAll();

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
     * Removes the instance of com.communote.server.persistence.user.group.ExternalUserGroup from
     * the persistent store.
     */
    public void remove(com.communote.server.model.user.group.ExternalUserGroup externalUserGroup);

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove(
            java.util.Collection<com.communote.server.model.user.group.ExternalUserGroup> entities);

    /**
     * Removes the instance of com.communote.server.persistence.user.group.ExternalUserGroup having
     * the given <code>identifier</code> from the persistent store.
     */
    public void remove(Long id);

    /**
     * Updates the <code>externalUserGroup</code> instance in the persistent store.
     */
    public void update(com.communote.server.model.user.group.ExternalUserGroup externalUserGroup);

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update(
            java.util.Collection<com.communote.server.model.user.group.ExternalUserGroup> entities);

}
