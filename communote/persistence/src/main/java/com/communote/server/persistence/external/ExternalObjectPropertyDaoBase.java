package com.communote.server.persistence.external;

import com.communote.server.model.external.ExternalObjectProperty;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ExternalObjectProperty</code>.
 * </p>
 *
 * @see ExternalObjectProperty
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ExternalObjectPropertyDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        ExternalObjectPropertyDao {

    /**
     * @see ExternalObjectPropertyDao#create(ExternalObjectProperty)
     */
    @Override
    public ExternalObjectProperty create(ExternalObjectProperty externalObjectProperty) {
        return (ExternalObjectProperty) this.create(TRANSFORM_NONE, externalObjectProperty);
    }

    /**
     * @see ExternalObjectPropertyDao#create(int transform, ExternalObjectProperty)
     */
    @Override
    public Object create(final int transform, final ExternalObjectProperty externalObjectProperty) {
        if (externalObjectProperty == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectProperty.create - 'externalObjectProperty' can not be null");
        }
        this.getHibernateTemplate().save(externalObjectProperty);
        return this.transformEntity(transform, externalObjectProperty);
    }

    /**
     * @see ExternalObjectPropertyDao#create(int, java.util.Collection<ExternalObjectProperty>)
     */
    @Override
    public java.util.Collection<ExternalObjectProperty> create(final int transform,
            final java.util.Collection<ExternalObjectProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectProperty.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<ExternalObjectProperty>() {
                    @Override
                    public ExternalObjectProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<ExternalObjectProperty> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see ExternalObjectPropertyDao#create(java.util. Collection<ExternalObjectProperty>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<ExternalObjectProperty> create(
            final java.util.Collection<ExternalObjectProperty> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(ExternalObjectProperty entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see ExternalObjectPropertyDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ExternalObjectProperty.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(ExternalObjectProperty.class, id);
        return transformEntity(transform, (ExternalObjectProperty) entity);
    }

    /**
     * @see ExternalObjectPropertyDao#load(Long)
     */
    @Override
    public ExternalObjectProperty load(Long id) {
        return (ExternalObjectProperty) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see ExternalObjectPropertyDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<ExternalObjectProperty> loadAll() {
        return (java.util.Collection<ExternalObjectProperty>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see ExternalObjectPropertyDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                ExternalObjectProperty.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see ExternalObjectPropertyDao#remove(ExternalObjectProperty)
     */
    @Override
    public void remove(ExternalObjectProperty externalObjectProperty) {
        if (externalObjectProperty == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectProperty.remove - 'externalObjectProperty' can not be null");
        }
        this.getHibernateTemplate().delete(externalObjectProperty);
    }

    /**
     * @see ExternalObjectPropertyDao#remove(java.util. Collection<ExternalObjectProperty>)
     */
    @Override
    public void remove(java.util.Collection<ExternalObjectProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectProperty.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see ExternalObjectPropertyDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectProperty.remove - 'id' can not be null");
        }
        ExternalObjectProperty entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,ExternalObjectProperty)} method. This method does not instantiate
     * a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>ExternalObjectPropertyDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,ExternalObjectProperty)
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
     * <code>ExternalObjectPropertyDao</code>, please note that the {@link #TRANSFORM_NONE} constant
     * denotes no transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link ExternalObjectPropertyDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final ExternalObjectProperty entity) {
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
     * @see ExternalObjectPropertyDao#update(ExternalObjectProperty)
     */
    @Override
    public void update(ExternalObjectProperty externalObjectProperty) {
        if (externalObjectProperty == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectProperty.update - 'externalObjectProperty' can not be null");
        }
        this.getHibernateTemplate().update(externalObjectProperty);
    }

    /**
     * @see ExternalObjectPropertyDao#update(java.util. Collection<ExternalObjectProperty>)
     */
    @Override
    public void update(final java.util.Collection<ExternalObjectProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ExternalObjectProperty.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<ExternalObjectProperty>() {
                    @Override
                    public ExternalObjectProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<ExternalObjectProperty> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

}