package com.communote.server.persistence.tag;

/**
 * @see com.communote.server.model.tag.GlobalTagCategory
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface GlobalTagCategoryDao {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes no transformation will occur.
     */
    public final static int TRANSFORM_NONE = 0;

    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes entities must be transformed into objects of type
     * {@link com.communote.server.core.vo.tag.GlobalTagCategoryVO}.
     */
    public final static int TRANSFORM_GLOBALTAGCATEGORYVO = 1;

    /**
     * Creates an instance of com.communote.server.persistence.tag.GlobalTagCategory and adds it to
     * the persistent store.
     */
    public com.communote.server.model.tag.GlobalTagCategory create(
            com.communote.server.model.tag.GlobalTagCategory globalTagCategory);

    /**
     * <p>
     * Does the same thing as {@link #create(com.communote.server.model.tag.GlobalTagCategory)} with
     * an additional flag called <code>transform</code>. If this flag is set to
     * <code>TRANSFORM_NONE</code> then the returned entity will <strong>NOT</strong> be
     * transformed. If this flag is any of the other constants defined here then the result
     * <strong>WILL BE</strong> passed through an operation which can optionally transform the
     * entity (into a value object for example). By default, transformation does not occur.
     * </p>
     */
    public Object create(int transform,
            com.communote.server.model.tag.GlobalTagCategory globalTagCategory);

    /**
     * <p>
     * Does the same thing as {@link #create(com.communote.server.model.tag.GlobalTagCategory)} with
     * an additional flag called <code>transform</code>. If this flag is set to
     * <code>TRANSFORM_NONE</code> then the returned entity will <strong>NOT</strong> be
     * transformed. If this flag is any of the other constants defined here then the result
     * <strong>WILL BE</strong> passed through an operation which can optionally transform the
     * entities (into value objects for example). By default, transformation does not occur.
     * </p>
     */
    public java.util.Collection<?> create(int transform,
            java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> entities);

    /**
     * Creates a new instance of com.communote.server.persistence.tag.GlobalTagCategory and adds
     * from the passed in <code>entities</code> collection
     * 
     * @param entities
     *            the collection of com.communote.server.persistence.tag.GlobalTagCategory instances
     *            to create.
     * 
     * @return the created instances.
     */
    public java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> create(
            java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> entities);

    /**
     * Evicts (removes) the entity from the hibernate cache
     * 
     * @param entity
     *            the entity to evict
     */
    public void evict(com.communote.server.model.tag.GlobalTagCategory entity);

    /**
     * <p>
     * Does the same thing as {@link #findByName(String)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then finder
     * results will <strong>NOT</strong> be transformed during retrieval. If this flag is any of the
     * other constants defined here then finder results <strong>WILL BE</strong> passed through an
     * operation which can optionally transform the entities (into value objects for example). By
     * default, transformation does not occur.
     * </p>
     */
    public Object findByName(int transform, String name);

    /**
     * <p>
     * Does the same thing as {@link #findByName(boolean, String)} with an additional argument
     * called <code>queryString</code>. This <code>queryString</code> argument allows you to
     * override the query string defined in {@link #findByName(int, String name)}.
     * </p>
     */
    public Object findByName(int transform, String queryString, String name);

    /**
 * 
     */
    public com.communote.server.model.tag.GlobalTagCategory findByName(String name);

    /**
     * <p>
     * Does the same thing as {@link #findByName(String)} with an additional argument called
     * <code>queryString</code>. This <code>queryString</code> argument allows you to override the
     * query string defined in {@link #findByName(String)}.
     * </p>
     */
    public com.communote.server.model.tag.GlobalTagCategory findByName(String queryString,
            String name);

    /**
     * <p>
     * Does the same thing as {@link #findByPrefix(String)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then finder
     * results will <strong>NOT</strong> be transformed during retrieval. If this flag is any of the
     * other constants defined here then finder results <strong>WILL BE</strong> passed through an
     * operation which can optionally transform the entities (into value objects for example). By
     * default, transformation does not occur.
     * </p>
     */
    public Object findByPrefix(int transform, String prefix);

    /**
     * <p>
     * Does the same thing as {@link #findByPrefix(boolean, String)} with an additional argument
     * called <code>queryString</code>. This <code>queryString</code> argument allows you to
     * override the query string defined in {@link #findByPrefix(int, String prefix)}.
     * </p>
     */
    public Object findByPrefix(int transform, String queryString, String prefix);

    /**
 * 
     */
    public com.communote.server.model.tag.GlobalTagCategory findByPrefix(String prefix);

    /**
     * <p>
     * Does the same thing as {@link #findByPrefix(String)} with an additional argument called
     * <code>queryString</code>. This <code>queryString</code> argument allows you to override the
     * query string defined in {@link #findByPrefix(String)}.
     * </p>
     */
    public com.communote.server.model.tag.GlobalTagCategory findByPrefix(String queryString,
            String prefix);

    /**
     * Converts an instance of type {@link com.communote.server.core.vo.tag.GlobalTagCategoryVO} to
     * this DAO's entity.
     */
    public com.communote.server.model.tag.GlobalTagCategory globalTagCategoryVOToEntity(
            com.communote.server.core.vo.tag.GlobalTagCategoryVO globalTagCategoryVO);

    /**
     * Copies the fields of {@link com.communote.server.core.vo.tag.GlobalTagCategoryVO} to the
     * specified entity.
     * 
     * @param copyIfNull
     *            If FALSE, the value object's field will not be copied to the entity if the value
     *            is NULL. If TRUE, it will be copied regardless of its value.
     */
    public void globalTagCategoryVOToEntity(
            com.communote.server.core.vo.tag.GlobalTagCategoryVO source,
            com.communote.server.model.tag.GlobalTagCategory target, boolean copyIfNull);

    /**
     * Converts a Collection of instances of type
     * {@link com.communote.server.core.vo.tag.GlobalTagCategoryVO} to this DAO's entity.
     */
    public void globalTagCategoryVOToEntityCollection(java.util.Collection instances);

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
     * Loads an instance of com.communote.server.persistence.tag.GlobalTagCategory from the
     * persistent store.
     */
    public com.communote.server.model.tag.GlobalTagCategory load(Long id);

    /**
     * Loads all entities of type {@link com.communote.server.model.tag.GlobalTagCategory}.
     * 
     * @return the loaded entities.
     */
    public java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> loadAll();

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
     * Removes the instance of com.communote.server.persistence.tag.GlobalTagCategory from the
     * persistent store.
     */
    public void remove(com.communote.server.model.tag.GlobalTagCategory globalTagCategory);

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove(
            java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> entities);

    /**
     * Removes the instance of com.communote.server.persistence.tag.GlobalTagCategory having the
     * given <code>identifier</code> from the persistent store.
     */
    public void remove(Long id);

    /**
     * Converts this DAO's entity to an object of type
     * {@link com.communote.server.core.vo.tag.GlobalTagCategoryVO}.
     */
    public com.communote.server.core.vo.tag.GlobalTagCategoryVO toGlobalTagCategoryVO(
            com.communote.server.model.tag.GlobalTagCategory entity);

    /**
     * Copies the fields of the specified entity to the target value object. This method is similar
     * to toGlobalTagCategoryVO(), but it does not handle any attributes in the target value object
     * that are "read-only" (as those do not have setter methods exposed).
     */
    public void toGlobalTagCategoryVO(com.communote.server.model.tag.GlobalTagCategory source,
            com.communote.server.core.vo.tag.GlobalTagCategoryVO target);

    /**
     * Converts this DAO's entity to a Collection of instances of type
     * {@link com.communote.server.core.vo.tag.GlobalTagCategoryVO}.
     */
    public void toGlobalTagCategoryVOCollection(java.util.Collection entities);

    /**
     * Updates the <code>globalTagCategory</code> instance in the persistent store.
     */
    public void update(com.communote.server.model.tag.GlobalTagCategory globalTagCategory);

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update(
            java.util.Collection<com.communote.server.model.tag.GlobalTagCategory> entities);

}
