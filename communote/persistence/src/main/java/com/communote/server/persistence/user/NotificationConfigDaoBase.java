package com.communote.server.persistence.user;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.NotificationConfig</code>.
 * </p>
 * 
 * @see com.communote.server.model.user.NotificationConfig
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class NotificationConfigDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.NotificationConfigDao {

    /**
     * @see com.communote.server.persistence.user.NotificationConfigDao#create(com.communote.server.model.user.NotificationConfig)
     */
    public com.communote.server.model.user.NotificationConfig create(
            com.communote.server.model.user.NotificationConfig notificationConfig) {
        return (com.communote.server.model.user.NotificationConfig) this.create(TRANSFORM_NONE,
                notificationConfig);
    }

    /**
     * @see com.communote.server.persistence.user.NotificationConfigDao#create(int transform,
     *      com.communote.server.persistence.user.NotificationConfig)
     */
    public Object create(final int transform,
            final com.communote.server.model.user.NotificationConfig notificationConfig) {
        if (notificationConfig == null) {
            throw new IllegalArgumentException(
                    "NotificationConfig.create - 'notificationConfig' can not be null");
        }
        this.getHibernateTemplate().save(notificationConfig);
        return this.transformEntity(transform, notificationConfig);
    }

    /**
     * @see com.communote.server.persistence.user.NotificationConfigDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.NotificationConfig>)
     */
    public java.util.Collection<com.communote.server.model.user.NotificationConfig> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.NotificationConfig> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "NotificationConfig.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.NotificationConfig>() {
                            public com.communote.server.model.user.NotificationConfig doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.NotificationConfig> entityIterator = entities
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
     *      com.communote.server.persistence.user.NotificationConfigDao#create(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.user.NotificationConfig>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.NotificationConfig> create(
            final java.util.Collection<com.communote.server.model.user.NotificationConfig> entities) {
        return (java.util.Collection<com.communote.server.model.user.NotificationConfig>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.user.NotificationConfig entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.NotificationConfigDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("NotificationConfig.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.NotificationConfigImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.user.NotificationConfig) entity);
    }

    /**
     * @see com.communote.server.persistence.user.NotificationConfigDao#load(Long)
     */
    public com.communote.server.model.user.NotificationConfig load(Long id) {
        return (com.communote.server.model.user.NotificationConfig) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.NotificationConfigDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.NotificationConfig> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.NotificationConfig>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.NotificationConfigDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.NotificationConfigImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.NotificationConfigDao#remove(com.communote.server.model.user.NotificationConfig)
     */
    public void remove(com.communote.server.model.user.NotificationConfig notificationConfig) {
        if (notificationConfig == null) {
            throw new IllegalArgumentException(
                    "NotificationConfig.remove - 'notificationConfig' can not be null");
        }
        this.getHibernateTemplate().delete(notificationConfig);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.NotificationConfigDao#remove(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.user.NotificationConfig>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.user.NotificationConfig> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "NotificationConfig.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.NotificationConfigDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("NotificationConfig.remove - 'id' can not be null");
        }
        com.communote.server.model.user.NotificationConfig entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.NotificationConfig)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.NotificationConfigDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.NotificationConfig)
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
     * <code>com.communote.server.persistence.user.NotificationConfigDao</code>, please note that
     * the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.NotificationConfigDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.NotificationConfig entity) {
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
     * @see com.communote.server.persistence.user.NotificationConfigDao#update(com.communote.server.model.user.NotificationConfig)
     */
    public void update(com.communote.server.model.user.NotificationConfig notificationConfig) {
        if (notificationConfig == null) {
            throw new IllegalArgumentException(
                    "NotificationConfig.update - 'notificationConfig' can not be null");
        }
        this.getHibernateTemplate().update(notificationConfig);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.NotificationConfigDao#update(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.user.NotificationConfig>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.user.NotificationConfig> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "NotificationConfig.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.NotificationConfig>() {
                            public com.communote.server.model.user.NotificationConfig doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.NotificationConfig> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}