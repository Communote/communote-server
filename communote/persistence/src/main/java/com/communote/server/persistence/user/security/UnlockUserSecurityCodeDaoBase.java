package com.communote.server.persistence.user.security;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.security.UnlockUserSecurityCode</code>.
 * </p>
 * 
 * @see com.communote.server.model.user.security.UnlockUserSecurityCode
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UnlockUserSecurityCodeDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao {

    /**
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#create(com.communote.server.model.user.security.UnlockUserSecurityCode)
     */
    public com.communote.server.model.user.security.UnlockUserSecurityCode create(
            com.communote.server.model.user.security.UnlockUserSecurityCode unlockUserSecurityCode) {
        return (com.communote.server.model.user.security.UnlockUserSecurityCode) this.create(
                TRANSFORM_NONE, unlockUserSecurityCode);
    }

    /**
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#create(int
     *      transform, com.communote.server.persistence.user.security.UnlockUserSecurityCode)
     */
    public Object create(
            final int transform,
            final com.communote.server.model.user.security.UnlockUserSecurityCode unlockUserSecurityCode) {
        if (unlockUserSecurityCode == null) {
            throw new IllegalArgumentException(
                    "UnlockUserSecurityCode.create - 'unlockUserSecurityCode' can not be null");
        }
        this.getHibernateTemplate().save(unlockUserSecurityCode);
        return this.transformEntity(transform, unlockUserSecurityCode);
    }

    /**
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#create(int,
     *      java
     *      .util.Collection<com.communote.server.persistence.user.security.UnlockUserSecurityCode>)
     */
    public java.util.Collection<com.communote.server.model.user.security.UnlockUserSecurityCode> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.security.UnlockUserSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "UnlockUserSecurityCode.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.security.UnlockUserSecurityCode>() {
                            public com.communote.server.model.user.security.UnlockUserSecurityCode doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.security.UnlockUserSecurityCode> entityIterator = entities
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
     *      com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#create(java.util
     *      .Collection<com.communote.server.persistence.user.security.UnlockUserSecurityCode>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.security.UnlockUserSecurityCode> create(
            final java.util.Collection<com.communote.server.model.user.security.UnlockUserSecurityCode> entities) {
        return (java.util.Collection<com.communote.server.model.user.security.UnlockUserSecurityCode>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#createCode(com.communote.server.model.user.User,
     *      com.communote.server.model.security.ChannelType)
     */
    public com.communote.server.model.user.security.UnlockUserSecurityCode createCode(
            final com.communote.server.model.user.User user,
            final com.communote.server.model.security.ChannelType channel) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao.createCode(com.communote.server.persistence.user.KenmeiUser user, com.communote.server.persistence.security.ChannelType channel) - 'user' can not be null");
        }
        if (channel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao.createCode(com.communote.server.persistence.user.KenmeiUser user, com.communote.server.persistence.security.ChannelType channel) - 'channel' can not be null");
        }
        try {
            return this.handleCreateCode(user, channel);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao.createCode(com.communote.server.persistence.user.KenmeiUser user, com.communote.server.persistence.security.ChannelType channel)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.user.security.UnlockUserSecurityCode entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#findBySecurityCode(String)
     */
    public com.communote.server.model.user.security.UnlockUserSecurityCode findBySecurityCode(
            final String code) {
        if (code == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao.findBySecurityCode(String code) - 'code' can not be null");
        }
        try {
            return this.handleFindBySecurityCode(code);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao.findBySecurityCode(String code)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#findByUserAndChannel(Long,
     *      com.communote.server.model.security.ChannelType)
     */
    public com.communote.server.model.user.security.UnlockUserSecurityCode findByUserAndChannel(
            final Long userId, final com.communote.server.model.security.ChannelType channel) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao.findByUserAndChannel(Long userId, com.communote.server.persistence.security.ChannelType channel) - 'userId' can not be null");
        }
        if (channel == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao.findByUserAndChannel(Long userId, com.communote.server.persistence.security.ChannelType channel) - 'channel' can not be null");
        }
        try {
            return this.handleFindByUserAndChannel(userId, channel);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao.findByUserAndChannel(Long userId, com.communote.server.persistence.security.ChannelType channel)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #createCode(com.communote.server.model.user.User, com.communote.server.model.security.ChannelType)}
     */
    protected abstract com.communote.server.model.user.security.UnlockUserSecurityCode handleCreateCode(
            com.communote.server.model.user.User user,
            com.communote.server.model.security.ChannelType channel);

    /**
     * Performs the core logic for {@link #findBySecurityCode(String)}
     */
    protected abstract com.communote.server.model.user.security.UnlockUserSecurityCode handleFindBySecurityCode(
            String code);

    /**
     * Performs the core logic for
     * {@link #findByUserAndChannel(Long, com.communote.server.model.security.ChannelType)}
     */
    protected abstract com.communote.server.model.user.security.UnlockUserSecurityCode handleFindByUserAndChannel(
            Long userId, com.communote.server.model.security.ChannelType channel);

    /**
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UnlockUserSecurityCode.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.security.UnlockUserSecurityCodeImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.user.security.UnlockUserSecurityCode) entity);
    }

    /**
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#load(Long)
     */
    public com.communote.server.model.user.security.UnlockUserSecurityCode load(Long id) {
        return (com.communote.server.model.user.security.UnlockUserSecurityCode) this.load(
                TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.security.UnlockUserSecurityCode> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.security.UnlockUserSecurityCode>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.security.UnlockUserSecurityCodeImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#remove(com.communote.server.model.user.security.UnlockUserSecurityCode)
     */
    public void remove(
            com.communote.server.model.user.security.UnlockUserSecurityCode unlockUserSecurityCode) {
        if (unlockUserSecurityCode == null) {
            throw new IllegalArgumentException(
                    "UnlockUserSecurityCode.remove - 'unlockUserSecurityCode' can not be null");
        }
        this.getHibernateTemplate().delete(unlockUserSecurityCode);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#remove(java.util
     *      .Collection<com.communote.server.persistence.user.security.UnlockUserSecurityCode>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.user.security.UnlockUserSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "UnlockUserSecurityCode.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "UnlockUserSecurityCode.remove - 'id' can not be null");
        }
        com.communote.server.model.user.security.UnlockUserSecurityCode entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.security.UnlockUserSecurityCode)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.security.UnlockUserSecurityCode)
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
     * <code>com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao</code>, please
     * note that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity
     * itself will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.security.UnlockUserSecurityCode entity) {
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
     * @see com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#update(com.communote.server.model.user.security.UnlockUserSecurityCode)
     */
    public void update(
            com.communote.server.model.user.security.UnlockUserSecurityCode unlockUserSecurityCode) {
        if (unlockUserSecurityCode == null) {
            throw new IllegalArgumentException(
                    "UnlockUserSecurityCode.update - 'unlockUserSecurityCode' can not be null");
        }
        this.getHibernateTemplate().update(unlockUserSecurityCode);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao#update(java.util
     *      .Collection<com.communote.server.persistence.user.security.UnlockUserSecurityCode>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.user.security.UnlockUserSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "UnlockUserSecurityCode.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.security.UnlockUserSecurityCode>() {
                            public com.communote.server.model.user.security.UnlockUserSecurityCode doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.security.UnlockUserSecurityCode> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}