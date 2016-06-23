package com.communote.server.persistence.messaging.config;

import com.communote.server.model.messaging.MessagerConnectorConfig;

/**
 * @see MessagerConnectorConfig
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface MessagerConnectorConfigDao {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes no transformation will occur.
     */
    public final static int TRANSFORM_NONE = 0;

    /**
     * <p>
     * Does the same thing as {@link #create(MessagerConnectorConfig)} with an additional flag
     * called <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the
     * returned entity will <strong>NOT</strong> be transformed. If this flag is any of the other
     * constants defined here then the result <strong>WILL BE</strong> passed through an operation
     * which can optionally transform the entities (into value objects for example). By default,
     * transformation does not occur.
     * </p>
     */
    public java.util.Collection<?> create(int transform,
            java.util.Collection<MessagerConnectorConfig> entities);

    /**
     * <p>
     * Does the same thing as {@link #create(MessagerConnectorConfig)} with an additional flag
     * called <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the
     * returned entity will <strong>NOT</strong> be transformed. If this flag is any of the other
     * constants defined here then the result <strong>WILL BE</strong> passed through an operation
     * which can optionally transform the entity (into a value object for example). By default,
     * transformation does not occur.
     * </p>
     */
    public Object create(int transform, MessagerConnectorConfig messagerConnectorConfig);

    /**
     * Creates a new instance of MessagerConnectorConfig and
     * adds from the passed in <code>entities</code> collection
     *
     * @param entities
     *            the collection of MessagerConnectorConfig
     *            instances to create.
     *
     * @return the created instances.
     */
    public java.util.Collection<MessagerConnectorConfig> create(
            java.util.Collection<MessagerConnectorConfig> entities);

    /**
     * Creates an instance of MessagerConnectorConfig and adds
     * it to the persistent store.
     */
    public MessagerConnectorConfig create(MessagerConnectorConfig messagerConnectorConfig);

    /**
     * Evicts (removes) the entity from the hibernate cache
     *
     * @param entity
     *            the entity to evict
     */
    public void evict(MessagerConnectorConfig entity);

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
     * Loads an instance of MessagerConnectorConfig from the
     * persistent store.
     */
    public MessagerConnectorConfig load(Long id);

    /**
     * Loads all entities of type {@link MessagerConnectorConfig}.
     *
     * @return the loaded entities.
     */
    public java.util.Collection<MessagerConnectorConfig> loadAll();

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
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove(java.util.Collection<MessagerConnectorConfig> entities);

    /**
     * Removes the instance of MessagerConnectorConfig having
     * the given <code>identifier</code> from the persistent store.
     */
    public void remove(Long id);

    /**
     * Removes the instance of MessagerConnectorConfig from
     * the persistent store.
     */
    public void remove(MessagerConnectorConfig messagerConnectorConfig);

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update(java.util.Collection<MessagerConnectorConfig> entities);

    /**
     * Updates the <code>messagerConnectorConfig</code> instance in the persistent store.
     */
    public void update(MessagerConnectorConfig messagerConnectorConfig);

}
