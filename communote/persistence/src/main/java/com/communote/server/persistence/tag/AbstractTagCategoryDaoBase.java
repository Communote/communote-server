package com.communote.server.persistence.tag;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.tag.AbstractTagCategory</code>.
 * </p>
 * 
 * @see com.communote.server.model.tag.AbstractTagCategory
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractTagCategoryDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.tag.AbstractTagCategoryDao {

    /**
     * @see com.communote.server.persistence.tag.AbstractTagCategoryDao#create(com.communote.server.model.tag.AbstractTagCategory)
     */
    public com.communote.server.model.tag.AbstractTagCategory create(
            com.communote.server.model.tag.AbstractTagCategory abstractTagCategory) {
        return (com.communote.server.model.tag.AbstractTagCategory) this.create(TRANSFORM_NONE,
                abstractTagCategory);
    }

    /**
     * @see com.communote.server.persistence.tag.AbstractTagCategoryDao#create(int transform,
     *      com.communote.server.persistence.tag.AbstractTagCategory)
     */
    public Object create(final int transform,
            final com.communote.server.model.tag.AbstractTagCategory abstractTagCategory) {
        if (abstractTagCategory == null) {
            throw new IllegalArgumentException(
                    "AbstractTagCategory.create - 'abstractTagCategory' can not be null");
        }
        this.getHibernateTemplate().save(abstractTagCategory);
        return this.transformEntity(transform, abstractTagCategory);
    }

    /**
     * @see com.communote.server.persistence.tag.AbstractTagCategoryDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.tag.AbstractTagCategory>)
     */
    public java.util.Collection<com.communote.server.model.tag.AbstractTagCategory> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.tag.AbstractTagCategory> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "AbstractTagCategory.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.tag.AbstractTagCategory>() {
                            public com.communote.server.model.tag.AbstractTagCategory doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.tag.AbstractTagCategory> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see 
     *      com.communote.server.persistence.tag.AbstractTagCategoryDao#create(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.tag.AbstractTagCategory>)
     */
    public java.util.Collection<com.communote.server.model.tag.AbstractTagCategory> create(
            final java.util.Collection<com.communote.server.model.tag.AbstractTagCategory> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.tag.AbstractTagCategory entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.tag.AbstractTagCategoryDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("AbstractTagCategory.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.tag.AbstractTagCategoryImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.tag.AbstractTagCategory) entity);
    }

    /**
     * @see com.communote.server.persistence.tag.AbstractTagCategoryDao#load(Long)
     */
    public com.communote.server.model.tag.AbstractTagCategory load(Long id) {
        return (com.communote.server.model.tag.AbstractTagCategory) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.tag.AbstractTagCategoryDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.tag.AbstractTagCategory> loadAll() {
        return (java.util.Collection<com.communote.server.model.tag.AbstractTagCategory>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.tag.AbstractTagCategoryDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.tag.AbstractTagCategoryImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.tag.AbstractTagCategoryDao#remove(com.communote.server.model.tag.AbstractTagCategory)
     */
    public void remove(com.communote.server.model.tag.AbstractTagCategory abstractTagCategory) {
        if (abstractTagCategory == null) {
            throw new IllegalArgumentException(
                    "AbstractTagCategory.remove - 'abstractTagCategory' can not be null");
        }
        this.getHibernateTemplate().delete(abstractTagCategory);
    }

    /**
     * @see 
     *      com.communote.server.persistence.tag.AbstractTagCategoryDao#remove(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.tag.AbstractTagCategory>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.tag.AbstractTagCategory> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "AbstractTagCategory.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.tag.AbstractTagCategoryDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("AbstractTagCategory.remove - 'id' can not be null");
        }
        com.communote.server.model.tag.AbstractTagCategory entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.tag.AbstractTagCategory)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.tag.AbstractTagCategoryDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.tag.AbstractTagCategory)
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
     * <code>com.communote.server.persistence.tag.AbstractTagCategoryDao</code>, please note that
     * the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.tag.AbstractTagCategoryDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.tag.AbstractTagCategory entity) {
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
     * @see com.communote.server.persistence.tag.AbstractTagCategoryDao#update(com.communote.server.model.tag.AbstractTagCategory)
     */
    public void update(com.communote.server.model.tag.AbstractTagCategory abstractTagCategory) {
        if (abstractTagCategory == null) {
            throw new IllegalArgumentException(
                    "AbstractTagCategory.update - 'abstractTagCategory' can not be null");
        }
        this.getHibernateTemplate().update(abstractTagCategory);
    }

    /**
     * @see 
     *      com.communote.server.persistence.tag.AbstractTagCategoryDao#update(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.tag.AbstractTagCategory>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.tag.AbstractTagCategory> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "AbstractTagCategory.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.tag.AbstractTagCategory>() {
                            public com.communote.server.model.tag.AbstractTagCategory doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.tag.AbstractTagCategory> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}