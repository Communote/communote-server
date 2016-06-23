package com.communote.server.persistence.user.security;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.security.AuthenticationFailedStatus</code>.
 * </p>
 *
 * @see com.communote.server.model.user.security.AuthenticationFailedStatus
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AuthenticationFailedStatusDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.security.AuthenticationFailedStatusDao {

    /**
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#create(com.communote.server.model.user.security.AuthenticationFailedStatus)
     */
    @Override
    public com.communote.server.model.user.security.AuthenticationFailedStatus create(
            com.communote.server.model.user.security.AuthenticationFailedStatus authenticationFailedStatus) {
        return (com.communote.server.model.user.security.AuthenticationFailedStatus) this.create(
                TRANSFORM_NONE, authenticationFailedStatus);
    }

    /**
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#create(int
     *      transform, com.communote.server.persistence.user.security.AuthenticationFailedStatus)
     */
    @Override
    public Object create(
            final int transform,
            final com.communote.server.model.user.security.AuthenticationFailedStatus authenticationFailedStatus) {
        if (authenticationFailedStatus == null) {
            throw new IllegalArgumentException(
                    "AuthenticationFailedStatus.create - 'authenticationFailedStatus' can not be null");
        }
        this.getHibernateTemplate().save(authenticationFailedStatus);
        return this.transformEntity(transform, authenticationFailedStatus);
    }

    /**
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.security.
     *      AuthenticationFailedStatus>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.user.security.AuthenticationFailedStatus> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.security.AuthenticationFailedStatus> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "AuthenticationFailedStatus.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.security.AuthenticationFailedStatus>() {
                            @Override
                            public com.communote.server.model.user.security.AuthenticationFailedStatus doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.security.AuthenticationFailedStatus> entityIterator = entities
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
     *      com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#create(java.
     *      util
     *      .Collection<com.communote.server.persistence.user.security.AuthenticationFailedStatus>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.security.AuthenticationFailedStatus> create(
            final java.util.Collection<com.communote.server.model.user.security.AuthenticationFailedStatus> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.user.security.AuthenticationFailedStatus entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#findByUserAndChannel(com.communote.server.model.user.User,
     *      com.communote.server.model.security.ChannelType)
     */
    @Override
    public com.communote.server.model.user.security.AuthenticationFailedStatus findByUserAndChannel(
            final com.communote.server.model.user.User user,
            final com.communote.server.model.security.ChannelType channel) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.AuthenticationFailedStatusDao.findByUserAndChannel(User user, ChannelType channel) - 'user' can not be null");
        }
        if (channel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.AuthenticationFailedStatusDao.findByUserAndChannel(User user, ChannelType channel) - 'channel' can not be null");
        }
        try {
            return this.handleFindByUserAndChannel(user, channel);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.AuthenticationFailedStatusDao.findByUserAndChannel(User user, ChannelType channel)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #findByUserAndChannel(com.communote.server.model.user.User, com.communote.server.model.security.ChannelType)}
     */
    protected abstract com.communote.server.model.user.security.AuthenticationFailedStatus handleFindByUserAndChannel(
            com.communote.server.model.user.User user,
            com.communote.server.model.security.ChannelType channel);

    /**
     * Performs the core logic for {@link #incFailedAuthCount(long)}
     */
    protected abstract void handleIncFailedAuthCount(long failedAuthStatusId);

    /**
     * Performs the core logic for {@link #updateLockedTimeout(long, java.sql.Timestamp)}
     */
    protected abstract void handleUpdateLockedTimeout(long failedAuthStatusId,
            java.sql.Timestamp lockedTimeout);

    /**
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#incFailedAuthCount(long)
     */
    @Override
    public void incFailedAuthCount(final long failedAuthStatusId) {
        try {
            this.handleIncFailedAuthCount(failedAuthStatusId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.AuthenticationFailedStatusDao.incFailedAuthCount(long failedAuthStatusId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#load(int,
     *      Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "AuthenticationFailedStatus.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.security.AuthenticationFailedStatusImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.user.security.AuthenticationFailedStatus) entity);
    }

    /**
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#load(Long)
     */
    @Override
    public com.communote.server.model.user.security.AuthenticationFailedStatus load(Long id) {
        return (com.communote.server.model.user.security.AuthenticationFailedStatus) this.load(
                TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.security.AuthenticationFailedStatus> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.security.AuthenticationFailedStatus>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.security.AuthenticationFailedStatusImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#remove(com.communote.server.model.user.security.AuthenticationFailedStatus)
     */
    @Override
    public void remove(
            com.communote.server.model.user.security.AuthenticationFailedStatus authenticationFailedStatus) {
        if (authenticationFailedStatus == null) {
            throw new IllegalArgumentException(
                    "AuthenticationFailedStatus.remove - 'authenticationFailedStatus' can not be null");
        }
        this.getHibernateTemplate().delete(authenticationFailedStatus);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#remove(java.
     *      util
     *      .Collection<com.communote.server.persistence.user.security.AuthenticationFailedStatus>)
     */
    @Override
    public void remove(
            java.util.Collection<com.communote.server.model.user.security.AuthenticationFailedStatus> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "AuthenticationFailedStatus.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "AuthenticationFailedStatus.remove - 'id' can not be null");
        }
        com.communote.server.model.user.security.AuthenticationFailedStatus entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.security.AuthenticationFailedStatus)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.security.AuthenticationFailedStatusDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.security.AuthenticationFailedStatus)
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
     * <code>com.communote.server.persistence.user.security.AuthenticationFailedStatusDao</code>,
     * please note that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the
     * entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.security.AuthenticationFailedStatusDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.security.AuthenticationFailedStatus entity) {
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
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#update(com.communote.server.model.user.security.AuthenticationFailedStatus)
     */
    @Override
    public void update(
            com.communote.server.model.user.security.AuthenticationFailedStatus authenticationFailedStatus) {
        if (authenticationFailedStatus == null) {
            throw new IllegalArgumentException(
                    "AuthenticationFailedStatus.update - 'authenticationFailedStatus' can not be null");
        }
        this.getHibernateTemplate().update(authenticationFailedStatus);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#update(java.
     *      util
     *      .Collection<com.communote.server.persistence.user.security.AuthenticationFailedStatus>)
     */
    @Override
    public void update(
            final java.util.Collection<com.communote.server.model.user.security.AuthenticationFailedStatus> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "AuthenticationFailedStatus.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.security.AuthenticationFailedStatus>() {
                            @Override
                            public com.communote.server.model.user.security.AuthenticationFailedStatus doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.security.AuthenticationFailedStatus> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

    /**
     * @see com.communote.server.persistence.user.security.AuthenticationFailedStatusDao#updateLockedTimeout(long,
     *      java.sql.Timestamp)
     */
    @Override
    public void updateLockedTimeout(final long failedAuthStatusId,
            final java.sql.Timestamp lockedTimeout) {
        if (lockedTimeout == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.AuthenticationFailedStatusDao.updateLockedTimeout(long failedAuthStatusId, java.sql.Timestamp lockedTimeout) - 'lockedTimeout' can not be null");
        }
        try {
            this.handleUpdateLockedTimeout(failedAuthStatusId, lockedTimeout);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.AuthenticationFailedStatusDao.updateLockedTimeout(long failedAuthStatusId, java.sql.Timestamp lockedTimeout)' --> "
                            + rt, rt);
        }
    }

}