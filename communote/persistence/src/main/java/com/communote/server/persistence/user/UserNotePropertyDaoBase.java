package com.communote.server.persistence.user;

import com.communote.server.model.user.UserNoteProperty;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>UserNoteProperty</code>.
 * </p>
 *
 * @see UserNoteProperty
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserNotePropertyDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        UserNotePropertyDao {

    /**
     * @see UserNotePropertyDao#create(int, java.util.Collection<UserNoteProperty>)
     */
    @Override
    public java.util.Collection<UserNoteProperty> create(final int transform,
            final java.util.Collection<UserNoteProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "UserNoteProperty.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<UserNoteProperty>() {
                    @Override
                    public UserNoteProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<UserNoteProperty> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see UserNotePropertyDao#create(int transform, UserNoteProperty)
     */
    @Override
    public Object create(final int transform, final UserNoteProperty userNoteProperty) {
        if (userNoteProperty == null) {
            throw new IllegalArgumentException(
                    "UserNoteProperty.create - 'userNoteProperty' can not be null");
        }
        this.getHibernateTemplate().save(userNoteProperty);
        return this.transformEntity(transform, userNoteProperty);
    }

    /**
     * @see UserNotePropertyDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.user.UserNoteProperty>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<UserNoteProperty> create(
            final java.util.Collection<UserNoteProperty> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see UserNotePropertyDao#create(UserNoteProperty)
     */
    @Override
    public UserNoteProperty create(UserNoteProperty userNoteProperty) {
        return (UserNoteProperty) this.create(TRANSFORM_NONE, userNoteProperty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(UserNoteProperty entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see UserNotePropertyDao#findProperties(Long, String, String)
     */
    @Override
    public java.util.Collection<UserNoteProperty> findProperties(final Long noteId,
            final String keyGroup, final String key) {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "UserNotePropertyDao.findProperties(Long noteId, String keyGroup, String key) - 'noteId' can not be null");
        }
        if (keyGroup == null) {
            throw new IllegalArgumentException(
                    "UserNotePropertyDao.findProperties(Long noteId, String keyGroup, String key) - 'keyGroup' can not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException(
                    "UserNotePropertyDao.findProperties(Long noteId, String keyGroup, String key) - 'key' can not be null");
        }
        try {
            return this.handleFindProperties(noteId, keyGroup, key);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'UserNotePropertyDao.findProperties(Long noteId, String keyGroup, String key)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see UserNotePropertyDao#findProperty(Long, String, String)
     */
    @Override
    public UserNoteProperty findProperty(final Long noteId, final String keyGroup, final String key) {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "UserNotePropertyDao.findProperty(Long noteId, String keyGroup, String key) - 'noteId' can not be null");
        }
        if (keyGroup == null) {
            throw new IllegalArgumentException(
                    "UserNotePropertyDao.findProperty(Long noteId, String keyGroup, String key) - 'keyGroup' can not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException(
                    "UserNotePropertyDao.findProperty(Long noteId, String keyGroup, String key) - 'key' can not be null");
        }
        try {
            return this.handleFindProperty(noteId, keyGroup, key);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'UserNotePropertyDao.findProperty(Long noteId, String keyGroup, String key)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #findProperties(Long, String, String)}
     */
    protected abstract java.util.Collection<UserNoteProperty> handleFindProperties(Long noteId,
            String keyGroup, String key);

    /**
     * Performs the core logic for {@link #findProperty(Long, String, String)}
     */
    protected abstract UserNoteProperty handleFindProperty(Long noteId, String keyGroup, String key);

    /**
     * Performs the core logic for {@link #removePropertiesForNote(Long)}
     */
    protected abstract int handleRemovePropertiesForNote(Long noteId);

    /**
     * Performs the core logic for {@link #removePropertiesForUser(Long)}
     */
    protected abstract int handleRemovePropertiesForUser(Long userId);

    /**
     * @see UserNotePropertyDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserNoteProperty.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(UserNoteProperty.class, id);
        return transformEntity(transform, (UserNoteProperty) entity);
    }

    /**
     * @see UserNotePropertyDao#load(Long)
     */
    @Override
    public UserNoteProperty load(Long id) {
        return (UserNoteProperty) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see UserNotePropertyDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<UserNoteProperty> loadAll() {
        return (java.util.Collection<UserNoteProperty>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see UserNotePropertyDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                UserNoteProperty.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see UserNotePropertyDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.user.UserNoteProperty>)
     */
    @Override
    public void remove(java.util.Collection<UserNoteProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "UserNoteProperty.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see UserNotePropertyDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserNoteProperty.remove - 'id' can not be null");
        }
        UserNoteProperty entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see UserNotePropertyDao#remove(UserNoteProperty)
     */
    @Override
    public void remove(UserNoteProperty userNoteProperty) {
        if (userNoteProperty == null) {
            throw new IllegalArgumentException(
                    "UserNoteProperty.remove - 'userNoteProperty' can not be null");
        }
        this.getHibernateTemplate().delete(userNoteProperty);
    }

    /**
     * @see UserNotePropertyDao#removePropertiesForNote(Long)
     */
    @Override
    public int removePropertiesForNote(final Long noteId) {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "UserNotePropertyDao.removePropertiesForNote(Long noteId) - 'noteId' can not be null");
        }
        try {
            return this.handleRemovePropertiesForNote(noteId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'UserNotePropertyDao.removePropertiesForNote(Long noteId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see UserNotePropertyDao#removePropertiesForUser(Long)
     */
    @Override
    public int removePropertiesForUser(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "UserNotePropertyDao.removePropertiesForUser(Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleRemovePropertiesForUser(userId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'UserNotePropertyDao.removePropertiesForUser(Long userId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,UserNoteProperty)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>UserNotePropertyDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,UserNoteProperty)
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
     * <code>UserNotePropertyDao</code>, please note that the {@link #TRANSFORM_NONE} constant
     * denotes no transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link UserNotePropertyDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final UserNoteProperty entity) {
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
     * @see UserNotePropertyDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.user.UserNoteProperty>)
     */
    @Override
    public void update(final java.util.Collection<UserNoteProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "UserNoteProperty.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<UserNoteProperty>() {
                    @Override
                    public UserNoteProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<UserNoteProperty> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

    /**
     * @see UserNotePropertyDao#update(UserNoteProperty)
     */
    @Override
    public void update(UserNoteProperty userNoteProperty) {
        if (userNoteProperty == null) {
            throw new IllegalArgumentException(
                    "UserNoteProperty.update - 'userNoteProperty' can not be null");
        }
        this.getHibernateTemplate().update(userNoteProperty);
    }

}