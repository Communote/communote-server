package com.communote.server.persistence.user.client;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.client.ClientStatistic</code>.
 * </p>
 * 
 * @see com.communote.server.model.client.ClientStatistic
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ClientStatisticDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.client.ClientStatisticDao {

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#create(com.communote.server.model.client.ClientStatistic)
     */
    public com.communote.server.model.client.ClientStatistic create(
            com.communote.server.model.client.ClientStatistic clientStatistic) {
        return (com.communote.server.model.client.ClientStatistic) this.create(TRANSFORM_NONE,
                clientStatistic);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#create(int transform,
     *      com.communote.server.persistence.user.client.ClientStatistic)
     */
    public Object create(final int transform,
            final com.communote.server.model.client.ClientStatistic clientStatistic) {
        if (clientStatistic == null) {
            throw new IllegalArgumentException(
                    "ClientStatistic.create - 'clientStatistic' can not be null");
        }
        this.getHibernateTemplate().save(clientStatistic);
        return this.transformEntity(transform, clientStatistic);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.client.ClientStatistic>)
     */
    public java.util.Collection<com.communote.server.model.client.ClientStatistic> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.client.ClientStatistic> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ClientStatistic.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.client.ClientStatistic>() {
                            public com.communote.server.model.client.ClientStatistic doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.client.ClientStatistic> entityIterator = entities
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
     *      com.communote.server.persistence.user.client.ClientStatisticDao#create(java.util.Collection
     *      <com.communote.server.persistence.user.client.ClientStatistic>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.client.ClientStatistic> create(
            final java.util.Collection<com.communote.server.model.client.ClientStatistic> entities) {
        return (java.util.Collection<com.communote.server.model.client.ClientStatistic>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#decrementRepositorySize(long)
     */
    public long decrementRepositorySize(final long value) {
        try {
            return this.handleDecrementRepositorySize(value);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.client.ClientStatisticDao.decrementRepositorySize(long value)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.client.ClientStatistic entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#getRepositorySize()
     */
    public long getRepositorySize() {
        try {
            return this.handleGetRepositorySize();
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.client.ClientStatisticDao.getRepositorySize()' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #decrementRepositorySize(long)}
     */
    protected abstract long handleDecrementRepositorySize(long value);

    /**
     * Performs the core logic for {@link #getRepositorySize()}
     */
    protected abstract long handleGetRepositorySize();

    /**
     * Performs the core logic for {@link #incrementRepositorySize(long)}
     */
    protected abstract long handleIncrementRepositorySize(long value);

    /**
     * Performs the core logic for {@link #initialise()}
     */
    protected abstract void handleInitialise();

    /**
     * Performs the core logic for {@link #resetRepositorySize()}
     */
    protected abstract void handleResetRepositorySize();

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#incrementRepositorySize(long)
     */
    public long incrementRepositorySize(final long value) {
        try {
            return this.handleIncrementRepositorySize(value);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.client.ClientStatisticDao.incrementRepositorySize(long value)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#initialise()
     */
    public void initialise() {
        try {
            this.handleInitialise();
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.client.ClientStatisticDao.initialise()' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ClientStatistic.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.client.ClientStatisticImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.client.ClientStatistic) entity);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#load(Long)
     */
    public com.communote.server.model.client.ClientStatistic load(Long id) {
        return (com.communote.server.model.client.ClientStatistic) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.client.ClientStatistic> loadAll() {
        return (java.util.Collection<com.communote.server.model.client.ClientStatistic>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.client.ClientStatisticImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#remove(com.communote.server.model.client.ClientStatistic)
     */
    public void remove(com.communote.server.model.client.ClientStatistic clientStatistic) {
        if (clientStatistic == null) {
            throw new IllegalArgumentException(
                    "ClientStatistic.remove - 'clientStatistic' can not be null");
        }
        this.getHibernateTemplate().delete(clientStatistic);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.client.ClientStatisticDao#remove(java.util.Collection
     *      <com.communote.server.persistence.user.client.ClientStatistic>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.client.ClientStatistic> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ClientStatistic.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ClientStatistic.remove - 'id' can not be null");
        }
        com.communote.server.model.client.ClientStatistic entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#resetRepositorySize()
     */
    public void resetRepositorySize() {
        try {
            this.handleResetRepositorySize();
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.client.ClientStatisticDao.resetRepositorySize()' --> "
                            + rt, rt);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.client.ClientStatistic)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.client.ClientStatisticDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.client.ClientStatistic)
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
     * <code>com.communote.server.persistence.user.client.ClientStatisticDao</code>, please note
     * that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself
     * will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.client.ClientStatisticDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.client.ClientStatistic entity) {
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
     * @see com.communote.server.persistence.user.client.ClientStatisticDao#update(com.communote.server.model.client.ClientStatistic)
     */
    public void update(com.communote.server.model.client.ClientStatistic clientStatistic) {
        if (clientStatistic == null) {
            throw new IllegalArgumentException(
                    "ClientStatistic.update - 'clientStatistic' can not be null");
        }
        this.getHibernateTemplate().update(clientStatistic);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.client.ClientStatisticDao#update(java.util.Collection
     *      <com.communote.server.persistence.user.client.ClientStatistic>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.client.ClientStatistic> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ClientStatistic.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.client.ClientStatistic>() {
                            public com.communote.server.model.client.ClientStatistic doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.client.ClientStatistic> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}