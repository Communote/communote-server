package com.communote.server.persistence.resource;

import com.communote.server.model.note.Content;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>Content</code>.
 * </p>
 *
 * @see Content
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ContentDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements ContentDao {

    /**
     * @see ContentDao#create(Content)
     */
    @Override
    public Content create(Content content) {
        return (Content) this.create(TRANSFORM_NONE, content);
    }

    /**
     * @see ContentDao#create(int transform, Content)
     */
    @Override
    public Object create(final int transform, final Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content.create - 'content' can not be null");
        }
        this.getHibernateTemplate().save(content);
        return this.transformEntity(transform, content);
    }

    /**
     * @see ContentDao#create(int, java.util.Collection<Content>)
     */
    @Override
    public java.util.Collection<Content> create(final int transform,
            final java.util.Collection<Content> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Content.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Content>() {
                    @Override
                    public Content doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<Content> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see ContentDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.resource.Content>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<Content> create(final java.util.Collection<Content> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(Content entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see ContentDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Content.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(Content.class, id);
        return transformEntity(transform, (Content) entity);
    }

    /**
     * @see ContentDao#load(Long)
     */
    @Override
    public Content load(Long id) {
        return (Content) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see ContentDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<Content> loadAll() {
        return (java.util.Collection<Content>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see ContentDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(Content.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see ContentDao#remove(Content)
     */
    @Override
    public void remove(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content.remove - 'content' can not be null");
        }
        this.getHibernateTemplate().delete(content);
    }

    /**
     * @see ContentDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.resource.Content>)
     */
    @Override
    public void remove(java.util.Collection<Content> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Content.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see ContentDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Content.remove - 'id' can not be null");
        }
        Content entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,Content)} method.
     * This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>ContentDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,Content)
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
     * <code>ContentDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes no
     * transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link ContentDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final Content entity) {
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
     * @see ContentDao#update(Content)
     */
    @Override
    public void update(Content content) {
        if (content == null) {
            throw new IllegalArgumentException("Content.update - 'content' can not be null");
        }
        this.getHibernateTemplate().update(content);
    }

    /**
     * @see ContentDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.resource.Content>)
     */
    @Override
    public void update(final java.util.Collection<Content> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Content.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Content>() {
                    @Override
                    public Content doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<Content> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

}