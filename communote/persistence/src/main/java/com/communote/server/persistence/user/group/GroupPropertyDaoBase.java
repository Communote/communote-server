package com.communote.server.persistence.user.group;

import com.communote.server.model.user.group.GroupProperty;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>GroupProperty</code>.
 * </p>
 *
 * @see GroupProperty
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class GroupPropertyDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements GroupPropertyDao {

    /**
     * @see GroupPropertyDao#create(GroupProperty)
     */
    @Override
    public GroupProperty create(GroupProperty entityGroupProperty) {
        return (GroupProperty) this.create(TRANSFORM_NONE, entityGroupProperty);
    }

    /**
     * @see GroupPropertyDao#create(int transform, GroupProperty)
     */
    @Override
    public Object create(final int transform, final GroupProperty entityGroupProperty) {
        if (entityGroupProperty == null) {
            throw new IllegalArgumentException(
                    "GroupProperty.create - 'entityGroupProperty' can not be null");
        }
        this.getHibernateTemplate().save(entityGroupProperty);
        return this.transformEntity(transform, entityGroupProperty);
    }

    /**
     * @see GroupPropertyDao#create(int, java.util.Collection<GroupProperty>)
     */
    @Override
    public java.util.Collection<GroupProperty> create(final int transform,
            final java.util.Collection<GroupProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("GroupProperty.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<GroupProperty>() {
                    @Override
                    public GroupProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<GroupProperty> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see GroupPropertyDao#create(java.util. Collection<GroupProperty>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<GroupProperty> create(
            final java.util.Collection<GroupProperty> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(GroupProperty entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see GroupPropertyDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("GroupProperty.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(GroupProperty.class, id);
        return transformEntity(transform, (GroupProperty) entity);
    }

    /**
     * @see GroupPropertyDao#load(Long)
     */
    @Override
    public GroupProperty load(Long id) {
        return (GroupProperty) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see GroupPropertyDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<GroupProperty> loadAll() {
        return (java.util.Collection<GroupProperty>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see GroupPropertyDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                GroupProperty.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see GroupPropertyDao#remove(GroupProperty)
     */
    @Override
    public void remove(GroupProperty entityGroupProperty) {
        if (entityGroupProperty == null) {
            throw new IllegalArgumentException(
                    "GroupProperty.remove - 'entityGroupProperty' can not be null");
        }
        this.getHibernateTemplate().delete(entityGroupProperty);
    }

    /**
     * @see GroupPropertyDao#remove(java.util. Collection<GroupProperty>)
     */
    @Override
    public void remove(java.util.Collection<GroupProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("GroupProperty.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see GroupPropertyDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("GroupProperty.remove - 'id' can not be null");
        }
        GroupProperty entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,GroupProperty)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>GroupPropertyDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,GroupProperty)
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
     * <code>GroupPropertyDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes
     * no transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link GroupPropertyDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final GroupProperty entity) {
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
     * @see GroupPropertyDao#update(GroupProperty)
     */
    @Override
    public void update(GroupProperty entityGroupProperty) {
        if (entityGroupProperty == null) {
            throw new IllegalArgumentException(
                    "GroupProperty.update - 'entityGroupProperty' can not be null");
        }
        this.getHibernateTemplate().update(entityGroupProperty);
    }

    /**
     * @see GroupPropertyDao#update(java.util. Collection<GroupProperty>)
     */
    @Override
    public void update(final java.util.Collection<GroupProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("GroupProperty.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<GroupProperty>() {
                    @Override
                    public GroupProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<GroupProperty> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

}