package com.communote.server.persistence.common.security;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>SecurityCode</code>.
 * </p>
 *
 * @see com.communote.server.model.security.SecurityCode
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class SecurityCodeDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.common.security.SecurityCodeDao {

    @Override
    public com.communote.server.model.security.SecurityCode create(
            com.communote.server.model.security.SecurityCode securityCode) {
        return (com.communote.server.model.security.SecurityCode) this.create(TRANSFORM_NONE,
                securityCode);
    }

    @Override
    public Object create(final int transform,
            final com.communote.server.model.security.SecurityCode securityCode) {
        if (securityCode == null) {
            throw new IllegalArgumentException(
                    "SecurityCode.create - 'securityCode' can not be null");
        }
        this.getHibernateTemplate().save(securityCode);
        return this.transformEntity(transform, securityCode);
    }

    @Override
    public java.util.Collection<com.communote.server.model.security.SecurityCode> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.security.SecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("SecurityCode.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.security.SecurityCode>() {
                            @Override
                            public com.communote.server.model.security.SecurityCode doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.security.SecurityCode> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.security.SecurityCode> create(
            final java.util.Collection<com.communote.server.model.security.SecurityCode> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    @Override
    public void deleteAllCodesByUser(final Long userId,
            final Class<? extends com.communote.server.model.security.SecurityCode> clazz) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "SecurityCodeDao.deleteAllCodesByUser(Long userId, Class<? extends SecurityCode> clazz) - 'userId' can not be null");
        }
        if (clazz == null) {
            throw new IllegalArgumentException(
                    "SecurityCodeDao.deleteAllCodesByUser(Long userId, Class<? extends SecurityCode> clazz) - 'clazz' can not be null");
        }
        try {
            this.handleDeleteAllCodesByUser(userId, clazz);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'SecurityCodeDao.deleteAllCodesByUser(Long userId, Class<? extends SecurityCode> clazz)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.security.SecurityCode entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.common.security.SecurityCodeDao#findByCode(String)
     */
    @Override
    public com.communote.server.model.security.SecurityCode findByCode(final String securityCode) {
        if (securityCode == null) {
            throw new IllegalArgumentException(
                    "SecurityCodeDao.findByCode(String securityCode) - 'securityCode' can not be null");
        }
        try {
            return this.handleFindByCode(securityCode);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'SecurityCodeDao.findByCode(String securityCode)' --> " + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for {@link #deleteAllCodesByUser(Long, Class<? extends
     * SecurityCode>)}
     */
    protected abstract void handleDeleteAllCodesByUser(Long userId,
            Class<? extends com.communote.server.model.security.SecurityCode> clazz);

    /**
     * Performs the core logic for {@link #findByCode(String)}
     */
    protected abstract com.communote.server.model.security.SecurityCode handleFindByCode(
            String securityCode);

    /**
     * @see com.communote.server.persistence.common.security.SecurityCodeDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("SecurityCode.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.security.SecurityCodeImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.security.SecurityCode) entity);
    }

    /**
     * @see com.communote.server.persistence.common.security.SecurityCodeDao#load(Long)
     */
    @Override
    public com.communote.server.model.security.SecurityCode load(Long id) {
        return (com.communote.server.model.security.SecurityCode) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.common.security.SecurityCodeDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.security.SecurityCode> loadAll() {
        return (java.util.Collection<com.communote.server.model.security.SecurityCode>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.common.security.SecurityCodeDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.security.SecurityCodeImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.common.security.SecurityCodeDao#remove(com.communote.server.model.security.SecurityCode)
     */
    @Override
    public void remove(com.communote.server.model.security.SecurityCode securityCode) {
        if (securityCode == null) {
            throw new IllegalArgumentException(
                    "SecurityCode.remove - 'securityCode' can not be null");
        }
        this.getHibernateTemplate().delete(securityCode);
    }

    @Override
    public void remove(
            java.util.Collection<com.communote.server.model.security.SecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("SecurityCode.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.common.security.SecurityCodeDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("SecurityCode.remove - 'id' can not be null");
        }
        com.communote.server.model.security.SecurityCode entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.security.SecurityCode)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>SecurityCodeDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.security.SecurityCode)
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
     * <code>SecurityCodeDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes
     * no transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.common.security.SecurityCodeDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.security.SecurityCode entity) {
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
     * @see com.communote.server.persistence.common.security.SecurityCodeDao#update(com.communote.server.model.security.SecurityCode)
     */
    @Override
    public void update(com.communote.server.model.security.SecurityCode securityCode) {
        if (securityCode == null) {
            throw new IllegalArgumentException(
                    "SecurityCode.update - 'securityCode' can not be null");
        }
        this.getHibernateTemplate().update(securityCode);
    }

    @Override
    public void update(
            final java.util.Collection<com.communote.server.model.security.SecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("SecurityCode.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.security.SecurityCode>() {
                            @Override
                            public com.communote.server.model.security.SecurityCode doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.security.SecurityCode> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}