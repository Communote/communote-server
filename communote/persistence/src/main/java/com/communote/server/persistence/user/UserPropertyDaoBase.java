package com.communote.server.persistence.user;

import com.communote.server.model.user.UserProperty;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>UserProperty</code>.
 * </p>
 *
 * @see UserProperty
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserPropertyDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements UserPropertyDao {

    /**
     * @see UserPropertyDao#create(int, java.util.Collection<UserProperty>)
     */
    @Override
    public java.util.Collection<UserProperty> create(final int transform,
            final java.util.Collection<UserProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserProperty.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<UserProperty>() {
                    @Override
                    public UserProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<UserProperty> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see UserPropertyDao#create(int transform, UserProperty)
     */
    @Override
    public Object create(final int transform, final UserProperty userProperty) {
        if (userProperty == null) {
            throw new IllegalArgumentException(
                    "UserProperty.create - 'userProperty' can not be null");
        }
        this.getHibernateTemplate().save(userProperty);
        return this.transformEntity(transform, userProperty);
    }

    /**
     * @see UserPropertyDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.user.UserProperty>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<UserProperty> create(
            final java.util.Collection<UserProperty> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see UserPropertyDao#create(UserProperty)
     */
    @Override
    public UserProperty create(UserProperty userProperty) {
        return (UserProperty) this.create(TRANSFORM_NONE, userProperty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(UserProperty entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see UserPropertyDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserProperty.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(UserProperty.class, id);
        return transformEntity(transform, (UserProperty) entity);
    }

    /**
     * @see UserPropertyDao#load(Long)
     */
    @Override
    public UserProperty load(Long id) {
        return (UserProperty) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see UserPropertyDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<UserProperty> loadAll() {
        return (java.util.Collection<UserProperty>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see UserPropertyDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                UserProperty.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see UserPropertyDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.user.UserProperty>)
     */
    @Override
    public void remove(java.util.Collection<UserProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserProperty.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see UserPropertyDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserProperty.remove - 'id' can not be null");
        }
        UserProperty entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see UserPropertyDao#remove(UserProperty)
     */
    @Override
    public void remove(UserProperty userProperty) {
        if (userProperty == null) {
            throw new IllegalArgumentException(
                    "UserProperty.remove - 'userProperty' can not be null");
        }
        this.getHibernateTemplate().delete(userProperty);
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,UserProperty)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>UserPropertyDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,UserProperty)
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
     * <code>UserPropertyDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes
     * no transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link UserPropertyDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final UserProperty entity) {
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
     * @see UserPropertyDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.user.UserProperty>)
     */
    @Override
    public void update(final java.util.Collection<UserProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserProperty.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<UserProperty>() {
                    @Override
                    public UserProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<UserProperty> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

    /**
     * @see UserPropertyDao#update(UserProperty)
     */
    @Override
    public void update(UserProperty userProperty) {
        if (userProperty == null) {
            throw new IllegalArgumentException(
                    "UserProperty.update - 'userProperty' can not be null");
        }
        this.getHibernateTemplate().update(userProperty);
    }

}