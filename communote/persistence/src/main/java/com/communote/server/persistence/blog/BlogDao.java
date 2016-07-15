package com.communote.server.persistence.blog;

/**
 * @see com.communote.server.model.blog.Blog
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface BlogDao {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into
     * value objects or other types, different methods in a class implementing this interface
     * support this feature: look for an <code>int</code> parameter called <code>transform</code>. <br>
     * This specific flag denotes no transformation will occur.
     */
    public final static int TRANSFORM_NONE = 0;

    /**
     * Creates an instance of com.communote.server.persistence.blog.Blog and adds it to the
     * persistent store.
     */
    public com.communote.server.model.blog.Blog create(com.communote.server.model.blog.Blog blog);

    /**
     * <p>
     * Does the same thing as {@link #create(com.communote.server.model.blog.Blog)} with an
     * additional flag called <code>transform</code>. If this flag is set to
     * <code>TRANSFORM_NONE</code> then the returned entity will <strong>NOT</strong> be
     * transformed. If this flag is any of the other constants defined here then the result
     * <strong>WILL BE</strong> passed through an operation which can optionally transform the
     * entity (into a value object for example). By default, transformation does not occur.
     * </p>
     */
    public Object create(int transform, com.communote.server.model.blog.Blog blog);

    /**
     * <p>
     * Does the same thing as {@link #create(com.communote.server.model.blog.Blog)} with an
     * additional flag called <code>transform</code>. If this flag is set to
     * <code>TRANSFORM_NONE</code> then the returned entity will <strong>NOT</strong> be
     * transformed. If this flag is any of the other constants defined here then the result
     * <strong>WILL BE</strong> passed through an operation which can optionally transform the
     * entities (into value objects for example). By default, transformation does not occur.
     * </p>
     */
    public java.util.Collection<?> create(int transform,
            java.util.Collection<com.communote.server.model.blog.Blog> entities);

    /**
     * Creates a new instance of com.communote.server.persistence.blog.Blog and adds from the passed
     * in <code>entities</code> collection
     *
     * @param entities
     *            the collection of com.communote.server.persistence.blog.Blog instances to create.
     *
     * @return the created instances.
     */
    public java.util.Collection<com.communote.server.model.blog.Blog> create(
            java.util.Collection<com.communote.server.model.blog.Blog> entities);

    /**
     * Evicts (removes) the entity from the hibernate cache
     *
     * @param entity
     *            the entity to evict
     */
    public void evict(com.communote.server.model.blog.Blog entity);

    /**
     *
     */
    public java.util.List<com.communote.server.model.blog.Blog> findBlogs(Long[] ids);

    /**
     * <p>
     * Return the blog that has the given external object assigned
     * </p>
     */
    public com.communote.server.model.blog.Blog findByExternalObject(Long internalExternalObjectId);

    /**
     * <p>
     * Find a blog that has a given external object
     * </p>
     */
    public com.communote.server.model.blog.Blog findByExternalObject(String externalSystemId,
            String externalId);

    /**
     * <p>
     * Return all blogs that have an external object with the given external system id
     * </p>
     */
    public java.util.List<com.communote.server.model.blog.Blog> findByExternalSystemId(
            String systemId);

    /**
     * <p>
     * Does the same thing as {@link #findByNameIdentifier(String)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then finder
     * results will <strong>NOT</strong> be transformed during retrieval. If this flag is any of the
     * other constants defined here then finder results <strong>WILL BE</strong> passed through an
     * operation which can optionally transform the entities (into value objects for example). By
     * default, transformation does not occur.
     * </p>
     */
    public Object findByNameIdentifier(int transform, String nameIdentifier);

    /**
     * <p>
     * Does the same thing as {@link #findByNameIdentifier(boolean, String)} with an additional
     * argument called <code>queryString</code>. This <code>queryString</code> argument allows you
     * to override the query string defined in {@link #findByNameIdentifier(int, String
     * nameIdentifier)}.
     * </p>
     */
    public Object findByNameIdentifier(int transform, String queryString, String nameIdentifier);

    /**
     * <p>
     * Find the blog with the given name identifier.
     * </p>
     */
    public com.communote.server.model.blog.Blog findByNameIdentifier(String nameIdentifier);

    /**
     * <p>
     * Does the same thing as {@link #findByNameIdentifier(String)} with an additional argument
     * called <code>queryString</code>. This <code>queryString</code> argument allows you to
     * override the query string defined in {@link #findByNameIdentifier(String)}.
     * </p>
     */
    public com.communote.server.model.blog.Blog findByNameIdentifier(String queryString,
            String nameIdentifier);

    /**
     * <p>
     * Returns all blogs that are managed by a user. This does only include blogs to which the user
     * was explicitly added as manager and not those the user can manage because of his membership
     * in a group with management access.
     * </p>
     */
    public java.util.List<com.communote.server.model.blog.Blog> findDirectlyManagedBlogsOfUser(
            Long userId);

    /**
     * <p>
     * Returns the Blog entity with the highest ID value.
     * </p>
     */
    public com.communote.server.model.blog.Blog findLatestBlog();

    /**
     * <p>
     * Gets the current count of blogs.
     * </p>
     */
    public long getBlogCount();

    /**
     *
     */
    public java.util.List<com.communote.server.api.core.blog.BlogData> getLastUsedBlogs(
            Long userId, int maxResult);

    /**
     *
     */
    public java.util.List<com.communote.server.api.core.blog.BlogData> getMostUsedBlogs(
            Long userId, int maxResults, int maxDays);

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
     * Loads an instance of com.communote.server.persistence.blog.Blog from the persistent store.
     */
    public com.communote.server.model.blog.Blog load(Long id);

    /**
     * Loads all entities of type {@link com.communote.server.model.blog.Blog}.
     *
     * @return the loaded entities.
     */
    public java.util.Collection<com.communote.server.model.blog.Blog> loadAll();

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
     * Removes the instance of com.communote.server.persistence.blog.Blog from the persistent store.
     */
    public void remove(com.communote.server.model.blog.Blog blog);

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove(java.util.Collection<com.communote.server.model.blog.Blog> entities);

    /**
     * Removes the instance of com.communote.server.persistence.blog.Blog having the given
     * <code>identifier</code> from the persistent store.
     */
    public void remove(Long id);

    /**
     * <p>
     * Removes "all read" and "all write" from all topics.
     * </p>
     */
    public void resetGlobalPermissions();

    /**
     * Updates the <code>blog</code> instance in the persistent store.
     */
    public void update(com.communote.server.model.blog.Blog blog);

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update(java.util.Collection<com.communote.server.model.blog.Blog> entities);

}
