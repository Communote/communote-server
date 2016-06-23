package com.communote.server.persistence.property;

import com.communote.server.model.property.Property;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>Property</code>.
 * </p>
 *
 * @see Property
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class PropertyDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements PropertyDao {

    /**
     * @see PropertyDao#create(int, java.util.Collection<Property>)
     */
    @Override
    public java.util.Collection<Property> create(final int transform,
            final java.util.Collection<Property> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Property.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Property>() {
                    @Override
                    public Property doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<Property> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see PropertyDao#create(int transform, Property)
     */
    @Override
    public Object create(final int transform, final Property property) {
        if (property == null) {
            throw new IllegalArgumentException("Property.create - 'property' can not be null");
        }
        this.getHibernateTemplate().save(property);
        return this.transformEntity(transform, property);
    }

    /**
     * @see PropertyDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.property.Property>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<Property> create(final java.util.Collection<Property> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see PropertyDao#create(Property)
     */
    @Override
    public Property create(Property property) {
        return (Property) this.create(TRANSFORM_NONE, property);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(Property entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see PropertyDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Property.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(Property.class, id);
        return transformEntity(transform, (Property) entity);
    }

    /**
     * @see PropertyDao#load(Long)
     */
    @Override
    public Property load(Long id) {
        return (Property) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see PropertyDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<Property> loadAll() {
        return (java.util.Collection<Property>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see PropertyDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(Property.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see PropertyDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.property.Property>)
     */
    @Override
    public void remove(java.util.Collection<Property> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Property.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see PropertyDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Property.remove - 'id' can not be null");
        }
        Property entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see PropertyDao#remove(Property)
     */
    @Override
    public void remove(Property property) {
        if (property == null) {
            throw new IllegalArgumentException("Property.remove - 'property' can not be null");
        }
        this.getHibernateTemplate().delete(property);
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,Property)} method.
     * This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>PropertyDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,Property)
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
     * <code>PropertyDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes no
     * transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link PropertyDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final Property entity) {
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
     * @see PropertyDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.property.Property>)
     */
    @Override
    public void update(final java.util.Collection<Property> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Property.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Property>() {
                    @Override
                    public Property doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<Property> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

    /**
     * @see PropertyDao#update(Property)
     */
    @Override
    public void update(Property property) {
        if (property == null) {
            throw new IllegalArgumentException("Property.update - 'property' can not be null");
        }
        this.getHibernateTemplate().update(property);
    }

}