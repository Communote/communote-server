package com.communote.server.persistence.user.group;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.group.UserOfGroup</code>.
 * </p>
 *
 * @see com.communote.server.model.user.group.UserOfGroup
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserOfGroupDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.group.UserOfGroupDao {

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#create(com.communote.server.model.user.group.UserOfGroup)
     */
    @Override
    public com.communote.server.model.user.group.UserOfGroup create(
            com.communote.server.model.user.group.UserOfGroup userOfGroup) {
        return (com.communote.server.model.user.group.UserOfGroup) this.create(TRANSFORM_NONE,
                userOfGroup);
    }

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#create(int transform,
     *      com.communote.server.persistence.user.group.UserOfGroup)
     */
    @Override
    public Object create(final int transform,
            final com.communote.server.model.user.group.UserOfGroup userOfGroup) {
        if (userOfGroup == null) {
            throw new IllegalArgumentException("UserOfGroup.create - 'userOfGroup' can not be null");
        }
        this.getHibernateTemplate().save(userOfGroup);
        return this.transformEntity(transform, userOfGroup);
    }

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.group.UserOfGroup>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.user.group.UserOfGroup> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.group.UserOfGroup> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserOfGroup.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.group.UserOfGroup>() {
                            @Override
                            public com.communote.server.model.user.group.UserOfGroup doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.group.UserOfGroup> entityIterator = entities
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
     *      com.communote.server.persistence.user.group.UserOfGroupDao#create(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.user.group.UserOfGroup>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.group.UserOfGroup> create(
            final java.util.Collection<com.communote.server.model.user.group.UserOfGroup> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.user.group.UserOfGroup entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#findByUserIdGroupId(Long,
     *      Long)
     */
    @Override
    public com.communote.server.model.user.group.UserOfGroup findByUserIdGroupId(final Long userId,
            final Long groupId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.group.UserOfGroupDao.findByUserIdGroupId(Long userId, Long groupId) - 'userId' can not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.group.UserOfGroupDao.findByUserIdGroupId(Long userId, Long groupId) - 'groupId' can not be null");
        }
        try {
            return this.handleFindByUserIdGroupId(userId, groupId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.group.UserOfGroupDao.findByUserIdGroupId(Long userId, Long groupId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#getUserOfGroupEntities(com.communote.server.model.user.group.UserOfGroupModificationType)
     */
    @Override
    public java.util.Collection<com.communote.server.model.user.group.UserOfGroup> getUserOfGroupEntities(
            final com.communote.server.model.user.group.UserOfGroupModificationType modificationType) {
        try {
            return this.handleGetUserOfGroupEntities(modificationType);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.group.UserOfGroupDao.getUserOfGroupEntities(UserOfGroupModificationType modificationType)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#getUsersOfGroup(Long)
     */
    @Override
    public java.util.Collection<Long> getUsersOfGroup(final Long groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.group.UserOfGroupDao.getUsersOfGroup(Long groupId) - 'groupId' can not be null");
        }
        try {
            return this.handleGetUsersOfGroup(groupId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.group.UserOfGroupDao.getUsersOfGroup(Long groupId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #findByUserIdGroupId(Long, Long)}
     */
    protected abstract com.communote.server.model.user.group.UserOfGroup handleFindByUserIdGroupId(
            Long userId, Long groupId);

    /**
     * Performs the core logic for
     * {@link #getUserOfGroupEntities(com.communote.server.model.user.group.UserOfGroupModificationType)}
     */
    protected abstract java.util.Collection<com.communote.server.model.user.group.UserOfGroup> handleGetUserOfGroupEntities(
            com.communote.server.model.user.group.UserOfGroupModificationType modificationType);

    /**
     * Performs the core logic for {@link #getUsersOfGroup(Long)}
     */
    protected abstract java.util.Collection<Long> handleGetUsersOfGroup(Long groupId);

    /**
     * Performs the core logic for {@link #isUserOfGroup(Long, Long)}
     */
    protected abstract boolean handleIsUserOfGroup(Long userId, Long groupId);

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#isUserOfGroup(Long, Long)
     */
    @Override
    public boolean isUserOfGroup(final Long userId, final Long groupId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.group.UserOfGroupDao.isUserOfGroup(Long userId, Long groupId) - 'userId' can not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.group.UserOfGroupDao.isUserOfGroup(Long userId, Long groupId) - 'groupId' can not be null");
        }
        try {
            return this.handleIsUserOfGroup(userId, groupId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.group.UserOfGroupDao.isUserOfGroup(Long userId, Long groupId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserOfGroup.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.group.UserOfGroupImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.user.group.UserOfGroup) entity);
    }

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#load(Long)
     */
    @Override
    public com.communote.server.model.user.group.UserOfGroup load(Long id) {
        return (com.communote.server.model.user.group.UserOfGroup) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.group.UserOfGroup> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.group.UserOfGroup>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.group.UserOfGroupImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#remove(com.communote.server.model.user.group.UserOfGroup)
     */
    @Override
    public void remove(com.communote.server.model.user.group.UserOfGroup userOfGroup) {
        if (userOfGroup == null) {
            throw new IllegalArgumentException("UserOfGroup.remove - 'userOfGroup' can not be null");
        }
        this.getHibernateTemplate().delete(userOfGroup);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.group.UserOfGroupDao#remove(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.user.group.UserOfGroup>)
     */
    @Override
    public void remove(
            java.util.Collection<com.communote.server.model.user.group.UserOfGroup> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserOfGroup.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserOfGroup.remove - 'id' can not be null");
        }
        com.communote.server.model.user.group.UserOfGroup entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.group.UserOfGroup)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.group.UserOfGroupDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.group.UserOfGroup)
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
     * <code>com.communote.server.persistence.user.group.UserOfGroupDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.group.UserOfGroupDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.group.UserOfGroup entity) {
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
     * @see com.communote.server.persistence.user.group.UserOfGroupDao#update(com.communote.server.model.user.group.UserOfGroup)
     */
    @Override
    public void update(com.communote.server.model.user.group.UserOfGroup userOfGroup) {
        if (userOfGroup == null) {
            throw new IllegalArgumentException("UserOfGroup.update - 'userOfGroup' can not be null");
        }
        this.getHibernateTemplate().update(userOfGroup);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.group.UserOfGroupDao#update(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.user.group.UserOfGroup>)
     */
    @Override
    public void update(
            final java.util.Collection<com.communote.server.model.user.group.UserOfGroup> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserOfGroup.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.group.UserOfGroup>() {
                            @Override
                            public com.communote.server.model.user.group.UserOfGroup doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.group.UserOfGroup> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}