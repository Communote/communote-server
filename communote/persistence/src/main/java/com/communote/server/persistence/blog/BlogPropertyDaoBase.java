package com.communote.server.persistence.blog;

import com.communote.server.model.blog.BlogProperty;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>BlogProperty</code>.
 * </p>
 *
 * @see BlogProperty
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class BlogPropertyDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements BlogPropertyDao {

    /**
     * @see BlogPropertyDao#create(BlogProperty)
     */
    @Override
    public BlogProperty create(BlogProperty blogProperty) {
        return (BlogProperty) this.create(TRANSFORM_NONE, blogProperty);
    }

    /**
     * @see BlogPropertyDao#create(int transform, BlogProperty)
     */
    @Override
    public Object create(final int transform, final BlogProperty blogProperty) {
        if (blogProperty == null) {
            throw new IllegalArgumentException(
                    "BlogProperty.create - 'blogProperty' can not be null");
        }
        this.getHibernateTemplate().save(blogProperty);
        return this.transformEntity(transform, blogProperty);
    }

    /**
     * @see BlogPropertyDao#create(int, java.util.Collection<BlogProperty>)
     */
    @Override
    public java.util.Collection<BlogProperty> create(final int transform,
            final java.util.Collection<BlogProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("BlogProperty.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<BlogProperty>() {
                    @Override
                    public BlogProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<BlogProperty> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see BlogPropertyDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.blog.BlogProperty>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<BlogProperty> create(
            final java.util.Collection<BlogProperty> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(BlogProperty entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see BlogPropertyDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("BlogProperty.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(BlogProperty.class, id);
        return transformEntity(transform, (BlogProperty) entity);
    }

    /**
     * @see BlogPropertyDao#load(Long)
     */
    @Override
    public BlogProperty load(Long id) {
        return (BlogProperty) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see BlogPropertyDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<BlogProperty> loadAll() {
        return (java.util.Collection<BlogProperty>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see BlogPropertyDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                BlogProperty.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see BlogPropertyDao#remove(BlogProperty)
     */
    @Override
    public void remove(BlogProperty blogProperty) {
        if (blogProperty == null) {
            throw new IllegalArgumentException(
                    "BlogProperty.remove - 'blogProperty' can not be null");
        }
        this.getHibernateTemplate().delete(blogProperty);
    }

    /**
     * @see BlogPropertyDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.blog.BlogProperty>)
     */
    @Override
    public void remove(java.util.Collection<BlogProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("BlogProperty.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see BlogPropertyDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("BlogProperty.remove - 'id' can not be null");
        }
        BlogProperty entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,BlogProperty)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>BlogPropertyDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,BlogProperty)
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
     * <code>BlogPropertyDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes
     * no transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link BlogPropertyDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final BlogProperty entity) {
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
     * @see BlogPropertyDao#update(BlogProperty)
     */
    @Override
    public void update(BlogProperty blogProperty) {
        if (blogProperty == null) {
            throw new IllegalArgumentException(
                    "BlogProperty.update - 'blogProperty' can not be null");
        }
        this.getHibernateTemplate().update(blogProperty);
    }

    /**
     * @see BlogPropertyDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.blog.BlogProperty>)
     */
    @Override
    public void update(final java.util.Collection<BlogProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("BlogProperty.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<BlogProperty>() {
                    @Override
                    public BlogProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<BlogProperty> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

}