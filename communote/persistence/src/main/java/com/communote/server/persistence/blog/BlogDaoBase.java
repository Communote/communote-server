package com.communote.server.persistence.blog;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogConstants;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>Blog</code>.
 * </p>
 *
 * @see Blog
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class BlogDaoBase extends HibernateDaoSupport implements BlogDao {

    private com.communote.server.persistence.global.GlobalIdDao globalIdDao;

    /**
     * @see BlogDao#create(Blog)
     */
    @Override
    public Blog create(Blog blog) {
        return (Blog) this.create(TRANSFORM_NONE, blog);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogDao#create(int transform,
     *      com.communote.server.persistence.blog.Blog)
     */
    @Override
    public Object create(final int transform, final Blog blog) {
        if (blog == null) {
            throw new IllegalArgumentException("Blog.create - 'blog' can not be null");
        }
        this.getHibernateTemplate().save(blog);
        return this.transformEntity(transform, blog);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.blog.Blog>)
     */
    @Override
    public java.util.Collection<Blog> create(final int transform,
            final java.util.Collection<Blog> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Blog.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Blog>() {
                    @Override
                    public Blog doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<Blog> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.blog.BlogDao#create(java.util.Collection<de.communardo.
     *      kenmei.core.api.bo.blog.Blog>)
     */
    @Override
    public java.util.Collection<Blog> create(final java.util.Collection<Blog> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(Blog entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see BlogDao#findBlogs(Long[])
     */
    @Override
    public List<Blog> findBlogs(final Long[] ids) {
        if (ids == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.BlogDao.findBlogs(Long[] ids) - 'ids' can not be null");
        }
        try {
            return this.handleFindBlogs(ids);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'BlogDao.findBlogs(Long[] ids)' --> " + rt, rt);
        }
    }

    /**
     * @see BlogDao#findByExternalObject(Long)
     */
    @Override
    public Blog findByExternalObject(final Long internalExternalObjectId) {
        if (internalExternalObjectId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.BlogDao.findByExternalObject(Long internalExternalObjectId) - 'internalExternalObjectId' can not be null");
        }
        try {
            return this.handleFindByExternalObject(internalExternalObjectId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'BlogDao.findByExternalObject(Long internalExternalObjectId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogDao#findByExternalObject(String, String)
     */
    @Override
    public Blog findByExternalObject(final String externalSystemId, final String externalId) {
        if (externalSystemId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.BlogDao.findByExternalObject(String externalSystemId, String externalId) - 'externalSystemId' can not be null");
        }
        if (externalId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.BlogDao.findByExternalObject(String externalSystemId, String externalId) - 'externalId' can not be null");
        }
        try {
            return this.handleFindByExternalObject(externalSystemId, externalId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'BlogDao.findByExternalObject(String externalSystemId, String externalId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogDao#findByExternalSystemId(String)
     */
    @Override
    public List<Blog> findByExternalSystemId(final String systemId) {
        if (systemId == null) {
            throw new IllegalArgumentException(
                    "BlogDao.findByExternalSystemId(String systemId) - 'systemId' can not be null");
        }
        try {
            return this.handleFindByExternalSystemId(systemId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'BlogDao.findByExternalSystemId(String systemId)' --> " + rt,
                    rt);
        }
    }

    /**
     * @see BlogDao#findByNameIdentifier(int, String)
     */
    @Override
    public Object findByNameIdentifier(final int transform, final String nameIdentifier) {
        return this.findByNameIdentifier(transform, "from " + BlogConstants.CLASS_NAME
                + " as blog where blog.nameIdentifier = :nameIdentifier", nameIdentifier);
    }

    /**
     * @see BlogDao#findByNameIdentifier(int, String, String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object findByNameIdentifier(final int transform, final String queryString,
            final String nameIdentifier) {
        try {
            org.hibernate.Query queryObject = super.getSession(false).createQuery(queryString);
            queryObject.setParameter("nameIdentifier", nameIdentifier);
            java.util.Set results = new java.util.LinkedHashSet(queryObject.list());
            Object result = null;
            if (results != null) {
                if (results.size() > 1) {
                    throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                            "More than one instance of 'Blog"
                                    + "' was found when executing query --> '" + queryString + "'");
                } else if (results.size() == 1) {
                    result = results.iterator().next();
                }
            }
            result = transformEntity(transform, (Blog) result);
            return result;
        } catch (org.hibernate.HibernateException ex) {
            throw super.convertHibernateAccessException(ex);
        }
    }

    /**
     * @see BlogDao#findByNameIdentifier(String)
     */
    @Override
    public Blog findByNameIdentifier(String nameIdentifier) {
        return (Blog) this.findByNameIdentifier(TRANSFORM_NONE, nameIdentifier);
    }

    /**
     * @see BlogDao#findByNameIdentifier(String, String)
     */
    @Override
    public Blog findByNameIdentifier(final String queryString, final String nameIdentifier) {
        return (Blog) this.findByNameIdentifier(TRANSFORM_NONE, queryString, nameIdentifier);
    }

    /**
     * @see BlogDao#findDirectlyManagedBlogsOfUser(Long)
     */
    @Override
    public List<Blog> findDirectlyManagedBlogsOfUser(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.BlogDao.findDirectlyManagedBlogsOfUser(Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleFindDirectlyManagedBlogsOfUser(userId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'BlogDao.findDirectlyManagedBlogsOfUser(Long userId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogDao#findLatestBlog()
     */
    @Override
    public Blog findLatestBlog() {
        try {
            return this.handleFindLatestBlog();
        } catch (RuntimeException rt) {
            throw new RuntimeException("Error performing 'BlogDao.findLatestBlog()' --> " + rt, rt);
        }
    }

    /**
     * Gets the reference to <code>globalIdDao</code>.
     */
    protected com.communote.server.persistence.global.GlobalIdDao getGlobalIdDao() {
        return this.globalIdDao;
    }

    /**
     * @see BlogDao#getLastUsedBlogs(Long, int)
     */
    @Override
    public List<com.communote.server.api.core.blog.BlogData> getLastUsedBlogs(final Long userId,
            final int maxResult) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.BlogDao.getLastUsedBlogs(Long userId, int maxResult) - 'userId' can not be null");
        }
        try {
            return this.handleGetLastUsedBlogs(userId, maxResult);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'BlogDao.getLastUsedBlogs(Long userId, int maxResult)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see BlogDao#getMostUsedBlogs(Long, int, int)
     */
    @Override
    public List<com.communote.server.api.core.blog.BlogData> getMostUsedBlogs(final Long userId,
            final int maxResults, final int maxDays) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.BlogDao.getMostUsedBlogs(Long userId, int maxResults, int maxDays) - 'userId' can not be null");
        }
        try {
            return this.handleGetMostUsedBlogs(userId, maxResults, maxDays);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'BlogDao.getMostUsedBlogs(Long userId, int maxResults, int maxDays)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #findBlogs(Long[])}
     */
    protected abstract List<Blog> handleFindBlogs(Long[] ids);

    /**
     * Performs the core logic for {@link #findByExternalObject(Long)}
     */
    protected abstract Blog handleFindByExternalObject(Long internalExternalObjectId);

    /**
     * Performs the core logic for {@link #findByExternalObject(String, String)}
     */
    protected abstract Blog handleFindByExternalObject(String externalSystemId, String externalId);

    /**
     * Performs the core logic for {@link #findByExternalSystemId(String)}
     */
    protected abstract List<Blog> handleFindByExternalSystemId(String systemId);

    /**
     * Performs the core logic for {@link #findDirectlyManagedBlogsOfUser(Long)}
     */
    protected abstract List<Blog> handleFindDirectlyManagedBlogsOfUser(Long userId);

    /**
     * Performs the core logic for {@link #findLatestBlog()}
     */
    protected abstract Blog handleFindLatestBlog();

    /**
     * Performs the core logic for {@link #getLastUsedBlogs(Long, int)}
     */
    protected abstract List<com.communote.server.api.core.blog.BlogData> handleGetLastUsedBlogs(
            Long userId, int maxResult);

    /**
     * Performs the core logic for {@link #getMostUsedBlogs(Long, int, int)}
     */
    protected abstract List<com.communote.server.api.core.blog.BlogData> handleGetMostUsedBlogs(
            Long userId, int maxResults, int maxDays);

    /**
     * @see BlogDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Blog.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(Blog.class, id);
        return transformEntity(transform, (Blog) entity);
    }

    /**
     * @see BlogDao#load(Long)
     */
    @Override
    public Blog load(Long id) {
        return (Blog) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see BlogDao#loadAll()
     */
    @Override
    public java.util.Collection<Blog> loadAll() {
        return (java.util.Collection<Blog>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see BlogDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(Blog.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see BlogDao#remove(Blog)
     */
    @Override
    public void remove(Blog blog) {
        if (blog == null) {
            throw new IllegalArgumentException("Blog.remove - 'blog' can not be null");
        }
        this.getHibernateTemplate().delete(blog);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogDao#remove(java.util.Collection<de.communardo.
     *      kenmei.core.api.bo.blog.Blog>)
     */
    @Override
    public void remove(java.util.Collection<Blog> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Blog.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see BlogDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Blog.remove - 'id' can not be null");
        }
        Blog entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Sets the reference to <code>globalIdDao</code>.
     */
    public void setGlobalIdDao(com.communote.server.persistence.global.GlobalIdDao globalIdDao) {
        this.globalIdDao = globalIdDao;
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,Blog)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.blog.BlogDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,Blog)
     */
    protected void transformEntities(final int transform, final java.util.Collection<?> entities) {
        switch (transform) {
        case TRANSFORM_NONE: // fall-through
        default:
            // do nothing;
        }
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter),
     * when the <code>transform</code> flag is set to one of the constants defined in
     * <code>com.communote.server.persistence.blog.BlogDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link BlogDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final Blog entity) {
        Object target = null;
        if (entity != null) {
            switch (transform) {
            case TRANSFORM_NONE: // fall-through
            default:
                target = entity;
            }
        }
        return target;
    }

    /**
     * @see BlogDao#update(Blog)
     */
    @Override
    public void update(Blog blog) {
        if (blog == null) {
            throw new IllegalArgumentException("Blog.update - 'blog' can not be null");
        }
        this.getHibernateTemplate().update(blog);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogDao#update(java.util.Collection<de.communardo.
     *      kenmei.core.api.bo.blog.Blog>)
     */
    @Override
    public void update(final java.util.Collection<Blog> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Blog.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Blog>() {
                    @Override
                    public Blog doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<Blog> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

}