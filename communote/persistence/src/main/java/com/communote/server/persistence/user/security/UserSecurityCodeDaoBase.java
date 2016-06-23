package com.communote.server.persistence.user.security;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.security.UserSecurityCode</code>.
 * </p>
 * 
 * @see com.communote.server.model.user.security.UserSecurityCode
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserSecurityCodeDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.security.UserSecurityCodeDao {

    private com.communote.server.persistence.common.security.SecurityCodeDao securityCodeDao;

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#create(com.communote.server.model.user.security.UserSecurityCode)
     */
    public com.communote.server.model.user.security.UserSecurityCode create(
            com.communote.server.model.user.security.UserSecurityCode userSecurityCode) {
        return (com.communote.server.model.user.security.UserSecurityCode) this.create(
                TRANSFORM_NONE, userSecurityCode);
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#create(int transform,
     *      com.communote.server.persistence.user.security.UserSecurityCode)
     */
    public Object create(final int transform,
            final com.communote.server.model.user.security.UserSecurityCode userSecurityCode) {
        if (userSecurityCode == null) {
            throw new IllegalArgumentException(
                    "UserSecurityCode.create - 'userSecurityCode' can not be null");
        }
        this.getHibernateTemplate().save(userSecurityCode);
        return this.transformEntity(transform, userSecurityCode);
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.security.UserSecurityCode>)
     */
    public java.util.Collection<com.communote.server.model.user.security.UserSecurityCode> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.security.UserSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "UserSecurityCode.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.security.UserSecurityCode>() {
                            public com.communote.server.model.user.security.UserSecurityCode doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.security.UserSecurityCode> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#create(java.util.
     *      Collection<com.communote.server.persistence.user.security.UserSecurityCode>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.security.UserSecurityCode> create(
            final java.util.Collection<com.communote.server.model.user.security.UserSecurityCode> entities) {
        return (java.util.Collection<com.communote.server.model.user.security.UserSecurityCode>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#createCode(com.communote.server.model.user.User)
     */
    public com.communote.server.model.user.security.UserSecurityCode createCode(
            final com.communote.server.model.user.User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.UserSecurityCodeDao.createCode(com.communote.server.persistence.user.KenmeiUser user) - 'user' can not be null");
        }
        try {
            return this.handleCreateCode(user);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.UserSecurityCodeDao.createCode(com.communote.server.persistence.user.KenmeiUser user)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.user.security.UserSecurityCode entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#findBySecurityCode(String)
     */
    public com.communote.server.model.user.security.UserSecurityCode findBySecurityCode(
            final String code) {
        if (code == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.UserSecurityCodeDao.findBySecurityCode(String code) - 'code' can not be null");
        }
        try {
            return this.handleFindBySecurityCode(code);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.UserSecurityCodeDao.findBySecurityCode(String code)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#findByUser(Long,
     *      com.communote.server.model.security.SecurityCodeAction)
     */
    public com.communote.server.model.user.security.UserSecurityCode findByUser(final Long userId,
            final com.communote.server.model.security.SecurityCodeAction action) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.UserSecurityCodeDao.findByUser(Long userId, SecurityCodeAction action) - 'userId' can not be null");
        }
        if (action == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.UserSecurityCodeDao.findByUser(Long userId, SecurityCodeAction action) - 'action' can not be null");
        }
        try {
            return this.handleFindByUser(userId, action);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.UserSecurityCodeDao.findByUser(Long userId, SecurityCodeAction action)' --> "
                            + rt, rt);
        }
    }

    /**
     * Gets the reference to <code>securityCodeDao</code>.
     */
    protected com.communote.server.persistence.common.security.SecurityCodeDao getSecurityCodeDao() {
        return this.securityCodeDao;
    }

    /**
     * Performs the core logic for {@link #createCode(com.communote.server.model.user.User)}
     */
    protected abstract com.communote.server.model.user.security.UserSecurityCode handleCreateCode(
            com.communote.server.model.user.User user);

    /**
     * Performs the core logic for {@link #findBySecurityCode(String)}
     */
    protected abstract com.communote.server.model.user.security.UserSecurityCode handleFindBySecurityCode(
            String code);

    /**
     * Performs the core logic for
     * {@link #findByUser(Long, com.communote.server.model.security.SecurityCodeAction)}
     */
    protected abstract com.communote.server.model.user.security.UserSecurityCode handleFindByUser(
            Long userId, com.communote.server.model.security.SecurityCodeAction action);

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserSecurityCode.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.security.UserSecurityCodeImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.user.security.UserSecurityCode) entity);
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#load(Long)
     */
    public com.communote.server.model.user.security.UserSecurityCode load(Long id) {
        return (com.communote.server.model.user.security.UserSecurityCode) this.load(
                TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.security.UserSecurityCode> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.security.UserSecurityCode>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.security.UserSecurityCodeImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#remove(com.communote.server.model.user.security.UserSecurityCode)
     */
    public void remove(com.communote.server.model.user.security.UserSecurityCode userSecurityCode) {
        if (userSecurityCode == null) {
            throw new IllegalArgumentException(
                    "UserSecurityCode.remove - 'userSecurityCode' can not be null");
        }
        this.getHibernateTemplate().delete(userSecurityCode);
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#remove(java.util.
     *      Collection<com.communote.server.persistence.user.security.UserSecurityCode>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.user.security.UserSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "UserSecurityCode.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserSecurityCode.remove - 'id' can not be null");
        }
        com.communote.server.model.user.security.UserSecurityCode entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Sets the reference to <code>securityCodeDao</code>.
     */
    public void setSecurityCodeDao(
            com.communote.server.persistence.common.security.SecurityCodeDao securityCodeDao) {
        this.securityCodeDao = securityCodeDao;
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.security.UserSecurityCode)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.security.UserSecurityCodeDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.security.UserSecurityCode)
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
     * <code>com.communote.server.persistence.user.security.UserSecurityCodeDao</code>, please note
     * that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself
     * will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.security.UserSecurityCodeDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.security.UserSecurityCode entity) {
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
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#update(com.communote.server.model.user.security.UserSecurityCode)
     */
    public void update(com.communote.server.model.user.security.UserSecurityCode userSecurityCode) {
        if (userSecurityCode == null) {
            throw new IllegalArgumentException(
                    "UserSecurityCode.update - 'userSecurityCode' can not be null");
        }
        this.getHibernateTemplate().update(userSecurityCode);
    }

    /**
     * @see com.communote.server.persistence.user.security.UserSecurityCodeDao#update(java.util.
     *      Collection<com.communote.server.persistence.user.security.UserSecurityCode>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.user.security.UserSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "UserSecurityCode.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.security.UserSecurityCode>() {
                            public com.communote.server.model.user.security.UserSecurityCode doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.security.UserSecurityCode> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}