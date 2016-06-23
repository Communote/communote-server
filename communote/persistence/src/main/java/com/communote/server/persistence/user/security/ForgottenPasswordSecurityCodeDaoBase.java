package com.communote.server.persistence.user.security;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.security.ForgottenPasswordSecurityCode</code>.
 * </p>
 * 
 * @see com.communote.server.model.user.security.ForgottenPasswordSecurityCode
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ForgottenPasswordSecurityCodeDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao {

    /**
     * @see com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#create(com.communote.server.model.user.security.ForgottenPasswordSecurityCode)
     */
    public com.communote.server.model.user.security.ForgottenPasswordSecurityCode create(
            com.communote.server.model.user.security.ForgottenPasswordSecurityCode forgottenPasswordSecurityCode) {
        return (com.communote.server.model.user.security.ForgottenPasswordSecurityCode) this
                .create(TRANSFORM_NONE, forgottenPasswordSecurityCode);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#create(int
     *      transform, com.communote.server.persistence.user.security.ForgottenPasswordSecurityCode)
     */
    public Object create(
            final int transform,
            final com.communote.server.model.user.security.ForgottenPasswordSecurityCode forgottenPasswordSecurityCode) {
        if (forgottenPasswordSecurityCode == null) {
            throw new IllegalArgumentException(
                    "ForgottenPasswordSecurityCode.create - 'forgottenPasswordSecurityCode' can not be null");
        }
        this.getHibernateTemplate().save(forgottenPasswordSecurityCode);
        return this.transformEntity(transform, forgottenPasswordSecurityCode);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#create(int
     *      , java.util.Collection<com.communote.server.persistence.user.security.
     *      ForgottenPasswordSecurityCode>)
     */
    public java.util.Collection<com.communote.server.model.user.security.ForgottenPasswordSecurityCode> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.security.ForgottenPasswordSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ForgottenPasswordSecurityCode.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.security.ForgottenPasswordSecurityCode>() {
                            public com.communote.server.model.user.security.ForgottenPasswordSecurityCode doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.security.ForgottenPasswordSecurityCode> entityIterator = entities
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
     *      com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#create(java
     *      .util.Collection<com.communote.server.persistence.user.security.
     *      ForgottenPasswordSecurityCode>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.security.ForgottenPasswordSecurityCode> create(
            final java.util.Collection<com.communote.server.model.user.security.ForgottenPasswordSecurityCode> entities) {
        return (java.util.Collection<com.communote.server.model.user.security.ForgottenPasswordSecurityCode>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * @see com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#createCode(com.communote.server.model.user.User)
     */
    public com.communote.server.model.user.security.ForgottenPasswordSecurityCode createCode(
            final com.communote.server.model.user.User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao.createCode(com.communote.server.persistence.user.KenmeiUser user) - 'user' can not be null");
        }
        try {
            return this.handleCreateCode(user);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao.createCode(com.communote.server.persistence.user.KenmeiUser user)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.user.security.ForgottenPasswordSecurityCode entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * Performs the core logic for {@link #createCode(com.communote.server.model.user.User)}
     */
    protected abstract com.communote.server.model.user.security.ForgottenPasswordSecurityCode handleCreateCode(
            com.communote.server.model.user.User user);

    /**
     * @see com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#load(int,
     *      Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "ForgottenPasswordSecurityCode.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.security.ForgottenPasswordSecurityCodeImpl.class,
                id);
        return transformEntity(transform,
                (com.communote.server.model.user.security.ForgottenPasswordSecurityCode) entity);
    }

    /**
     * @see com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#load(Long)
     */
    public com.communote.server.model.user.security.ForgottenPasswordSecurityCode load(Long id) {
        return (com.communote.server.model.user.security.ForgottenPasswordSecurityCode) this.load(
                TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.security.ForgottenPasswordSecurityCode> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.security.ForgottenPasswordSecurityCode>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.security.ForgottenPasswordSecurityCodeImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#remove(com.communote.server.model.user.security.ForgottenPasswordSecurityCode)
     */
    public void remove(
            com.communote.server.model.user.security.ForgottenPasswordSecurityCode forgottenPasswordSecurityCode) {
        if (forgottenPasswordSecurityCode == null) {
            throw new IllegalArgumentException(
                    "ForgottenPasswordSecurityCode.remove - 'forgottenPasswordSecurityCode' can not be null");
        }
        this.getHibernateTemplate().delete(forgottenPasswordSecurityCode);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#remove(java
     *      .util.Collection<com.communote.server.persistence.user.security.
     *      ForgottenPasswordSecurityCode>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.user.security.ForgottenPasswordSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ForgottenPasswordSecurityCode.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "ForgottenPasswordSecurityCode.remove - 'id' can not be null");
        }
        com.communote.server.model.user.security.ForgottenPasswordSecurityCode entity = this
                .load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.security.ForgottenPasswordSecurityCode)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.security.ForgottenPasswordSecurityCode)
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
     * <code>com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao</code>,
     * please note that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the
     * entity itself will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.security.ForgottenPasswordSecurityCode entity) {
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
     * @see com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#update(com.communote.server.model.user.security.ForgottenPasswordSecurityCode)
     */
    public void update(
            com.communote.server.model.user.security.ForgottenPasswordSecurityCode forgottenPasswordSecurityCode) {
        if (forgottenPasswordSecurityCode == null) {
            throw new IllegalArgumentException(
                    "ForgottenPasswordSecurityCode.update - 'forgottenPasswordSecurityCode' can not be null");
        }
        this.getHibernateTemplate().update(forgottenPasswordSecurityCode);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao#update(java
     *      .util.Collection<com.communote.server.persistence.user.security.
     *      ForgottenPasswordSecurityCode>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.user.security.ForgottenPasswordSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ForgottenPasswordSecurityCode.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.security.ForgottenPasswordSecurityCode>() {
                            public com.communote.server.model.user.security.ForgottenPasswordSecurityCode doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.security.ForgottenPasswordSecurityCode> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}