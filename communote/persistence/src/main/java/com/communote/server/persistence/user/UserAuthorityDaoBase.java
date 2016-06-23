package com.communote.server.persistence.user;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>UserAuthority</code>.
 * </p>
 *
 * @see com.communote.server.model.user.UserAuthority
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserAuthorityDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.UserAuthorityDao {

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#create(com.communote.server.model.user.UserAuthority)
     */
    @Override
    public com.communote.server.model.user.UserAuthority create(
            com.communote.server.model.user.UserAuthority kenmeiUserAuthority) {
        return (com.communote.server.model.user.UserAuthority) this.create(TRANSFORM_NONE,
                kenmeiUserAuthority);
    }

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#create(int transform,
     *      UserAuthority)
     */
    @Override
    public Object create(final int transform,
            final com.communote.server.model.user.UserAuthority kenmeiUserAuthority) {
        if (kenmeiUserAuthority == null) {
            throw new IllegalArgumentException(
                    "UserAuthority.create - 'kenmeiUserAuthority' can not be null");
        }
        this.getHibernateTemplate().save(kenmeiUserAuthority);
        return this.transformEntity(transform, kenmeiUserAuthority);
    }

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#create(int,
     *      java.util.Collection<UserAuthority>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.user.UserAuthority> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.UserAuthority> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserAuthority.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.UserAuthority>() {
                            @Override
                            public com.communote.server.model.user.UserAuthority doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.UserAuthority> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#create(java.util.Collection<
     *      UserAuthority>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.UserAuthority> create(
            final java.util.Collection<com.communote.server.model.user.UserAuthority> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#createAuthorities(com.communote.server.model.user.UserRole[],
     *      boolean)
     */
    @Override
    public java.util.Set<com.communote.server.model.user.UserAuthority> createAuthorities(
            final com.communote.server.model.user.UserRole[] roles, final boolean save) {
        if (roles == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserAuthorityDao.createAuthorities(UserRole[] roles, boolean save) - 'roles' can not be null");
        }
        try {
            return this.handleCreateAuthorities(roles, save);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserAuthorityDao.createAuthorities(UserRole[] roles, boolean save)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.user.UserAuthority entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * Performs the core logic for
     * {@link #createAuthorities(com.communote.server.model.user.UserRole[], boolean)}
     */
    protected abstract java.util.Set<com.communote.server.model.user.UserAuthority> handleCreateAuthorities(
            com.communote.server.model.user.UserRole[] roles, boolean save);

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserAuthority.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.UserAuthority.class, id);
        return transformEntity(transform, (com.communote.server.model.user.UserAuthority) entity);
    }

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#load(Long)
     */
    @Override
    public com.communote.server.model.user.UserAuthority load(Long id) {
        return (com.communote.server.model.user.UserAuthority) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.UserAuthority> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.UserAuthority>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.UserAuthority.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#remove(com.communote.server.model.user.UserAuthority)
     */
    @Override
    public void remove(com.communote.server.model.user.UserAuthority kenmeiUserAuthority) {
        if (kenmeiUserAuthority == null) {
            throw new IllegalArgumentException(
                    "UserAuthority.remove - 'kenmeiUserAuthority' can not be null");
        }
        this.getHibernateTemplate().delete(kenmeiUserAuthority);
    }

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#remove(java.util.Collection<
     *      UserAuthority>)
     */
    @Override
    public void remove(java.util.Collection<com.communote.server.model.user.UserAuthority> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserAuthority.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserAuthority.remove - 'id' can not be null");
        }
        com.communote.server.model.user.UserAuthority entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.UserAuthority)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.UserAuthorityDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.UserAuthority)
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
     * <code>com.communote.server.persistence.user.UserAuthorityDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.UserAuthorityDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.UserAuthority entity) {
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
     * @see com.communote.server.persistence.user.UserAuthorityDao#update(com.communote.server.model.user.UserAuthority)
     */
    @Override
    public void update(com.communote.server.model.user.UserAuthority kenmeiUserAuthority) {
        if (kenmeiUserAuthority == null) {
            throw new IllegalArgumentException(
                    "UserAuthority.update - 'kenmeiUserAuthority' can not be null");
        }
        this.getHibernateTemplate().update(kenmeiUserAuthority);
    }

    /**
     * @see com.communote.server.persistence.user.UserAuthorityDao#update(java.util.Collection<
     *      UserAuthority>)
     */
    @Override
    public void update(
            final java.util.Collection<com.communote.server.model.user.UserAuthority> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserAuthority.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.UserAuthority>() {
                            @Override
                            public com.communote.server.model.user.UserAuthority doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.UserAuthority> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}