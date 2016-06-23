package com.communote.server.persistence.property;

import com.communote.server.model.property.StringProperty;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>StringProperty</code>.
 * </p>
 *
 * @see StringProperty
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class StringPropertyDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements StringPropertyDao {

    /**
     * @see StringPropertyDao#create(int, java.util.Collection<StringProperty>)
     */
    @Override
    public java.util.Collection<StringProperty> create(final int transform,
            final java.util.Collection<StringProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("StringProperty.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<StringProperty>() {
                    @Override
                    public StringProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<StringProperty> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see StringPropertyDao#create(int transform, StringProperty)
     */
    @Override
    public Object create(final int transform, final StringProperty stringProperty) {
        if (stringProperty == null) {
            throw new IllegalArgumentException(
                    "StringProperty.create - 'stringProperty' can not be null");
        }
        this.getHibernateTemplate().save(stringProperty);
        return this.transformEntity(transform, stringProperty);
    }

    /**
     * @see StringPropertyDao#create(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.property.StringProperty>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<StringProperty> create(
            final java.util.Collection<StringProperty> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see StringPropertyDao#create(StringProperty)
     */
    @Override
    public StringProperty create(StringProperty stringProperty) {
        return (StringProperty) this.create(TRANSFORM_NONE, stringProperty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(StringProperty entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see StringPropertyDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("StringProperty.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(StringProperty.class, id);
        return transformEntity(transform, (StringProperty) entity);
    }

    /**
     * @see StringPropertyDao#load(Long)
     */
    @Override
    public StringProperty load(Long id) {
        return (StringProperty) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see StringPropertyDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<StringProperty> loadAll() {
        return (java.util.Collection<StringProperty>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see StringPropertyDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                StringProperty.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see StringPropertyDao#remove(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.property.StringProperty>)
     */
    @Override
    public void remove(java.util.Collection<StringProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("StringProperty.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see StringPropertyDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("StringProperty.remove - 'id' can not be null");
        }
        StringProperty entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see StringPropertyDao#remove(StringProperty)
     */
    @Override
    public void remove(StringProperty stringProperty) {
        if (stringProperty == null) {
            throw new IllegalArgumentException(
                    "StringProperty.remove - 'stringProperty' can not be null");
        }
        this.getHibernateTemplate().delete(stringProperty);
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,StringProperty)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>StringPropertyDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,StringProperty)
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
     * <code>StringPropertyDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes
     * no transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link StringPropertyDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final StringProperty entity) {
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
     * @see StringPropertyDao#update(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.property.StringProperty>)
     */
    @Override
    public void update(final java.util.Collection<StringProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("StringProperty.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<StringProperty>() {
                    @Override
                    public StringProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<StringProperty> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

    /**
     * @see StringPropertyDao#update(StringProperty)
     */
    @Override
    public void update(StringProperty stringProperty) {
        if (stringProperty == null) {
            throw new IllegalArgumentException(
                    "StringProperty.update - 'stringProperty' can not be null");
        }
        this.getHibernateTemplate().update(stringProperty);
    }

}