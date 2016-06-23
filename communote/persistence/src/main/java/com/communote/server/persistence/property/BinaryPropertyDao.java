package com.communote.server.persistence.property;

import java.util.Collection;

import com.communote.server.core.vo.IdDateTO;
import com.communote.server.model.property.BinaryProperty;

/**
 * @see BinaryProperty
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface BinaryPropertyDao {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes no transformation will occur.
     */
    public final static int TRANSFORM_NONE = 0;

    /**
     * Creates an instance of BinaryProperty and adds it to the persistent store.
     */
    public BinaryProperty create(BinaryProperty binaryProperty);

    /**
     * Creates a new instance of BinaryProperty and adds from the passed in <code>entities</code>
     * collection
     * 
     * @param entities
     *            the collection of BinaryProperty instances to create.
     * 
     * @return the created instances.
     */
    public Collection<BinaryProperty> create(Collection<BinaryProperty> entities);

    /**
     * <p>
     * Does the same thing as {@link #create(BinaryProperty)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entity (into a value object for example). By default, transformation
     * does not occur.
     * </p>
     */
    public Object create(int transform, BinaryProperty binaryProperty);

    /**
     * <p>
     * Does the same thing as {@link #create(BinaryProperty)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned
     * entity will <strong>NOT</strong> be transformed. If this flag is any of the other constants
     * defined here then the result <strong>WILL BE</strong> passed through an operation which can
     * optionally transform the entities (into value objects for example). By default,
     * transformation does not occur.
     * </p>
     */
    public Collection<?> create(int transform, Collection<BinaryProperty> entities);

    /**
     * Evicts (removes) the entity from the hibernate cache
     * 
     * @param entity
     *            the entity to evict
     */
    public void evict(BinaryProperty entity);

    /**
     * Retrieve the ID of the property that has the given group and key.
     * 
     * @param keyGroup
     *            the group of the property to find
     * @param key
     *            the key of the property to find
     * @return the ID and last modification timestamp of the property or null if there is no
     *         property
     */
    public IdDateTO findIdByKey(String keyGroup, String key);

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
     * Loads an instance of BinaryProperty from the persistent store.
     */
    public BinaryProperty load(Long id);

    /**
     * Loads all entities of type {@link BinaryProperty}.
     * 
     * @return the loaded entities.
     */
    public Collection<BinaryProperty> loadAll();

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
     * Removes the instance of BinaryProperty from the persistent store.
     */
    public void remove(BinaryProperty binaryProperty);

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove(Collection<BinaryProperty> entities);

    /**
     * Removes the instance of BinaryProperty having the given <code>identifier</code> from the
     * persistent store.
     */
    public void remove(Long id);

    /**
     * Updates the <code>binaryProperty</code> instance in the persistent store.
     */
    public void update(BinaryProperty binaryProperty);

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update(Collection<BinaryProperty> entities);

}
