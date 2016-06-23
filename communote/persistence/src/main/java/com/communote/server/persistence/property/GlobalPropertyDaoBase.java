package com.communote.server.persistence.property;

import com.communote.server.model.property.GlobalProperty;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>GlobalProperty</code>.
 * </p>
 *
 * @see GlobalProperty
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class GlobalPropertyDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements GlobalPropertyDao {

    /**
     * @see GlobalPropertyDao#create(GlobalProperty)
     */
    @Override
    public GlobalProperty create(GlobalProperty globalProperty) {
        return (GlobalProperty) this.create(TRANSFORM_NONE, globalProperty);
    }

    /**
     * @see GlobalPropertyDao#create(int transform, GlobalProperty)
     */
    @Override
    public Object create(final int transform, final GlobalProperty globalProperty) {
        if (globalProperty == null) {
            throw new IllegalArgumentException(
                    "GlobalProperty.create - 'globalProperty' can not be null");
        }
        this.getHibernateTemplate().save(globalProperty);
        return this.transformEntity(transform, globalProperty);
    }

    /**
     * @see GlobalPropertyDao#create(int, java.util.Collection<GlobalProperty>)
     */
    @Override
    public java.util.Collection<GlobalProperty> create(final int transform,
            final java.util.Collection<GlobalProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("GlobalProperty.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<GlobalProperty>() {
                    @Override
                    public GlobalProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<GlobalProperty> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see GlobalPropertyDao#create(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.property.GlobalProperty>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<GlobalProperty> create(
            final java.util.Collection<GlobalProperty> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(GlobalProperty entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see GlobalPropertyDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("GlobalProperty.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(GlobalProperty.class, id);
        return transformEntity(transform, (GlobalProperty) entity);
    }

    /**
     * @see GlobalPropertyDao#load(Long)
     */
    @Override
    public GlobalProperty load(Long id) {
        return (GlobalProperty) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see GlobalPropertyDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<GlobalProperty> loadAll() {
        return (java.util.Collection<GlobalProperty>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see GlobalPropertyDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                GlobalProperty.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see GlobalPropertyDao#remove(GlobalProperty)
     */
    @Override
    public void remove(GlobalProperty globalProperty) {
        if (globalProperty == null) {
            throw new IllegalArgumentException(
                    "GlobalProperty.remove - 'globalProperty' can not be null");
        }
        this.getHibernateTemplate().delete(globalProperty);
    }

    /**
     * @see GlobalPropertyDao#remove(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.property.GlobalProperty>)
     */
    @Override
    public void remove(java.util.Collection<GlobalProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("GlobalProperty.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see GlobalPropertyDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("GlobalProperty.remove - 'id' can not be null");
        }
        GlobalProperty entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,GlobalProperty)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>GlobalPropertyDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,GlobalProperty)
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
     * <code>GlobalPropertyDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes
     * no transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link GlobalPropertyDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final GlobalProperty entity) {
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
     * @see GlobalPropertyDao#update(GlobalProperty)
     */
    @Override
    public void update(GlobalProperty globalProperty) {
        if (globalProperty == null) {
            throw new IllegalArgumentException(
                    "GlobalProperty.update - 'globalProperty' can not be null");
        }
        this.getHibernateTemplate().update(globalProperty);
    }

    /**
     * @see GlobalPropertyDao#update(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.property.GlobalProperty>)
     */
    @Override
    public void update(final java.util.Collection<GlobalProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("GlobalProperty.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<GlobalProperty>() {
                    @Override
                    public GlobalProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<GlobalProperty> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

}