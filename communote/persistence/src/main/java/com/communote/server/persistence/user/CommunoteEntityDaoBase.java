package com.communote.server.persistence.user;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.KenmeiEntity</code>.
 * </p>
 *
 * @see com.communote.server.model.user.CommunoteEntity
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class CommunoteEntityDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.CommunoteEntityDao {

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#create(com.communote.server.model.user.CommunoteEntity)
     */
    @Override
    public com.communote.server.model.user.CommunoteEntity create(
            com.communote.server.model.user.CommunoteEntity kenmeiEntity) {
        return (com.communote.server.model.user.CommunoteEntity) this.create(TRANSFORM_NONE,
                kenmeiEntity);
    }

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#create(int transform,
     *      com.communote.server.persistence.user.KenmeiEntity)
     */
    @Override
    public Object create(final int transform,
            final com.communote.server.model.user.CommunoteEntity kenmeiEntity) {
        if (kenmeiEntity == null) {
            throw new IllegalArgumentException(
                    "CommunoteEntity.create - 'kenmeiEntity' can not be null");
        }
        this.getHibernateTemplate().save(kenmeiEntity);
        return this.transformEntity(transform, kenmeiEntity);
    }

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.KenmeiEntity>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.user.CommunoteEntity> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.CommunoteEntity> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "CommunoteEntity.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.CommunoteEntity>() {
                            @Override
                            public com.communote.server.model.user.CommunoteEntity doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.CommunoteEntity> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.user.KenmeiEntity>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.CommunoteEntity> create(
            final java.util.Collection<com.communote.server.model.user.CommunoteEntity> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.user.CommunoteEntity entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * Performs the core logic for {@link #loadWithImplementation(Long)}
     */
    protected abstract com.communote.server.model.user.CommunoteEntity handleLoadWithImplementation(
            Long id);

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("CommunoteEntity.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.CommunoteEntity.class, id);
        return transformEntity(transform, (com.communote.server.model.user.CommunoteEntity) entity);
    }

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#load(Long)
     */
    @Override
    public com.communote.server.model.user.CommunoteEntity load(Long id) {
        return (com.communote.server.model.user.CommunoteEntity) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.CommunoteEntity> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.CommunoteEntity>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.CommunoteEntity.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#loadWithImplementation(Long)
     */
    @Override
    public com.communote.server.model.user.CommunoteEntity loadWithImplementation(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.CommunoteEntityDao.loadWithImplementation(Long id) - 'id' can not be null");
        }
        try {
            return this.handleLoadWithImplementation(id);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.CommunoteEntityDao.loadWithImplementation(Long id)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#remove(com.communote.server.model.user.CommunoteEntity)
     */
    @Override
    public void remove(com.communote.server.model.user.CommunoteEntity kenmeiEntity) {
        if (kenmeiEntity == null) {
            throw new IllegalArgumentException(
                    "CommunoteEntity.remove - 'kenmeiEntity' can not be null");
        }
        this.getHibernateTemplate().delete(kenmeiEntity);
    }

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.user.KenmeiEntity>)
     */
    @Override
    public void remove(
            java.util.Collection<com.communote.server.model.user.CommunoteEntity> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "CommunoteEntity.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("CommunoteEntity.remove - 'id' can not be null");
        }
        com.communote.server.model.user.CommunoteEntity entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.CommunoteEntity)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.CommunoteEntityDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.CommunoteEntity)
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
     * <code>com.communote.server.persistence.user.CommunoteEntityDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.CommunoteEntityDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.CommunoteEntity entity) {
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
     * @see com.communote.server.persistence.user.CommunoteEntityDao#update(com.communote.server.model.user.CommunoteEntity)
     */
    @Override
    public void update(com.communote.server.model.user.CommunoteEntity kenmeiEntity) {
        if (kenmeiEntity == null) {
            throw new IllegalArgumentException(
                    "CommunoteEntity.update - 'kenmeiEntity' can not be null");
        }
        this.getHibernateTemplate().update(kenmeiEntity);
    }

    /**
     * @see com.communote.server.persistence.user.CommunoteEntityDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.user.KenmeiEntity>)
     */
    @Override
    public void update(
            final java.util.Collection<com.communote.server.model.user.CommunoteEntity> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "CommunoteEntity.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.CommunoteEntity>() {
                            @Override
                            public com.communote.server.model.user.CommunoteEntity doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.CommunoteEntity> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}