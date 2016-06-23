package com.communote.server.persistence.user.security;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.security.EmailSecurityCode</code>.
 * </p>
 *
 * @see com.communote.server.model.user.security.EmailSecurityCode
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class EmailSecurityCodeDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.security.EmailSecurityCodeDao {

    private com.communote.server.persistence.common.security.SecurityCodeDao securityCodeDao;

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#create(com.communote.server.model.user.security.EmailSecurityCode)
     */
    @Override
    public com.communote.server.model.user.security.EmailSecurityCode create(
            com.communote.server.model.user.security.EmailSecurityCode emailSecurityCode) {
        return (com.communote.server.model.user.security.EmailSecurityCode) this.create(
                TRANSFORM_NONE, emailSecurityCode);
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#create(int
     *      transform, com.communote.server.persistence.user.security.EmailSecurityCode)
     */
    @Override
    public Object create(final int transform,
            final com.communote.server.model.user.security.EmailSecurityCode emailSecurityCode) {
        if (emailSecurityCode == null) {
            throw new IllegalArgumentException(
                    "EmailSecurityCode.create - 'emailSecurityCode' can not be null");
        }
        this.getHibernateTemplate().save(emailSecurityCode);
        return this.transformEntity(transform, emailSecurityCode);
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.security.EmailSecurityCode>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.user.security.EmailSecurityCode> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.security.EmailSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "EmailSecurityCode.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.security.EmailSecurityCode>() {
                            @Override
                            public com.communote.server.model.user.security.EmailSecurityCode doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.security.EmailSecurityCode> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#create(java.util.
     *      Collection<com.communote.server.persistence.user.security.EmailSecurityCode>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.security.EmailSecurityCode> create(
            final java.util.Collection<com.communote.server.model.user.security.EmailSecurityCode> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#createCode(com.communote.server.model.user.User,
     *      String)
     */
    @Override
    public com.communote.server.model.user.security.EmailSecurityCode createCode(
            final com.communote.server.model.user.User user, final String newEmail) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.EmailSecurityCodeDao.createCode(User user, String newEmail) - 'user' can not be null");
        }
        if (newEmail == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.EmailSecurityCodeDao.createCode(User user, String newEmail) - 'newEmail' can not be null");
        }
        try {
            return this.handleCreateCode(user, newEmail);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.EmailSecurityCodeDao.createCode(User user, String newEmail)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.user.security.EmailSecurityCode entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#findByEmailAddress(String)
     */
    @Override
    public com.communote.server.model.user.security.EmailSecurityCode findByEmailAddress(
            final String email) {
        if (email == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.EmailSecurityCodeDao.findByEmailAddress(String email) - 'email' can not be null");
        }
        try {
            return this.handleFindByEmailAddress(email);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.EmailSecurityCodeDao.findByEmailAddress(String email)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#findBySecurityCode(String)
     */
    @Override
    public com.communote.server.model.user.security.EmailSecurityCode findBySecurityCode(
            final String code) {
        if (code == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.EmailSecurityCodeDao.findBySecurityCode(String code) - 'code' can not be null");
        }
        try {
            return this.handleFindBySecurityCode(code);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.EmailSecurityCodeDao.findBySecurityCode(String code)' --> "
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
     * Performs the core logic for {@link #createCode(com.communote.server.model.user.User, String)}
     */
    protected abstract com.communote.server.model.user.security.EmailSecurityCode handleCreateCode(
            com.communote.server.model.user.User user, String newEmail);

    /**
     * Performs the core logic for {@link #findByEmailAddress(String)}
     */
    protected abstract com.communote.server.model.user.security.EmailSecurityCode handleFindByEmailAddress(
            String email);

    /**
     * Performs the core logic for {@link #findBySecurityCode(String)}
     */
    protected abstract com.communote.server.model.user.security.EmailSecurityCode handleFindBySecurityCode(
            String code);

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("EmailSecurityCode.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.security.EmailSecurityCodeImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.user.security.EmailSecurityCode) entity);
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#load(Long)
     */
    @Override
    public com.communote.server.model.user.security.EmailSecurityCode load(Long id) {
        return (com.communote.server.model.user.security.EmailSecurityCode) this.load(
                TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.security.EmailSecurityCode> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.security.EmailSecurityCode>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.security.EmailSecurityCodeImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#remove(com.communote.server.model.user.security.EmailSecurityCode)
     */
    @Override
    public void remove(com.communote.server.model.user.security.EmailSecurityCode emailSecurityCode) {
        if (emailSecurityCode == null) {
            throw new IllegalArgumentException(
                    "EmailSecurityCode.remove - 'emailSecurityCode' can not be null");
        }
        this.getHibernateTemplate().delete(emailSecurityCode);
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#remove(java.util.
     *      Collection<com.communote.server.persistence.user.security.EmailSecurityCode>)
     */
    @Override
    public void remove(
            java.util.Collection<com.communote.server.model.user.security.EmailSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "EmailSecurityCode.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("EmailSecurityCode.remove - 'id' can not be null");
        }
        com.communote.server.model.user.security.EmailSecurityCode entity = this.load(id);
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
     * {@link #transformEntity(int,com.communote.server.model.user.security.EmailSecurityCode)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.security.EmailSecurityCodeDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.security.EmailSecurityCode)
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
     * <code>com.communote.server.persistence.user.security.EmailSecurityCodeDao</code>, please note
     * that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself
     * will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.security.EmailSecurityCodeDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.security.EmailSecurityCode entity) {
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
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#update(com.communote.server.model.user.security.EmailSecurityCode)
     */
    @Override
    public void update(com.communote.server.model.user.security.EmailSecurityCode emailSecurityCode) {
        if (emailSecurityCode == null) {
            throw new IllegalArgumentException(
                    "EmailSecurityCode.update - 'emailSecurityCode' can not be null");
        }
        this.getHibernateTemplate().update(emailSecurityCode);
    }

    /**
     * @see com.communote.server.persistence.user.security.EmailSecurityCodeDao#update(java.util.
     *      Collection<com.communote.server.persistence.user.security.EmailSecurityCode>)
     */
    @Override
    public void update(
            final java.util.Collection<com.communote.server.model.user.security.EmailSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "EmailSecurityCode.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.security.EmailSecurityCode>() {
                            @Override
                            public com.communote.server.model.user.security.EmailSecurityCode doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.security.EmailSecurityCode> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}