package com.communote.server.persistence.config;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.config.ClientConfiguration</code>.
 * </p>
 * 
 * @see com.communote.server.model.config.ClientConfiguration
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ClientConfigurationDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.config.ClientConfigurationDao {

    /**
     * @see com.communote.server.persistence.config.ClientConfigurationDao#create(com.communote.server.model.config.ClientConfiguration)
     */
    public com.communote.server.model.config.ClientConfiguration create(
            com.communote.server.model.config.ClientConfiguration clientConfiguration) {
        return (com.communote.server.model.config.ClientConfiguration) this.create(TRANSFORM_NONE,
                clientConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.ClientConfigurationDao#create(int transform,
     *      com.communote.server.persistence.config.ClientConfiguration)
     */
    public Object create(final int transform,
            final com.communote.server.model.config.ClientConfiguration clientConfiguration) {
        if (clientConfiguration == null) {
            throw new IllegalArgumentException(
                    "ClientConfiguration.create - 'clientConfiguration' can not be null");
        }
        this.getHibernateTemplate().save(clientConfiguration);
        return this.transformEntity(transform, clientConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.ClientConfigurationDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.config.ClientConfiguration>)
     */
    public java.util.Collection<com.communote.server.model.config.ClientConfiguration> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.config.ClientConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ClientConfiguration.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.ClientConfiguration>() {
                            public com.communote.server.model.config.ClientConfiguration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.ClientConfiguration> entityIterator = entities
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
     *      com.communote.server.persistence.config.ClientConfigurationDao#create(java.util.Collection
     *      <com.communote.server.persistence.config.ClientConfiguration>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.ClientConfiguration> create(
            final java.util.Collection<com.communote.server.model.config.ClientConfiguration> entities) {
        return (java.util.Collection<com.communote.server.model.config.ClientConfiguration>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.config.ClientConfiguration entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.config.ClientConfigurationDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ClientConfiguration.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.config.ClientConfigurationImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.config.ClientConfiguration) entity);
    }

    /**
     * @see com.communote.server.persistence.config.ClientConfigurationDao#load(Long)
     */
    public com.communote.server.model.config.ClientConfiguration load(Long id) {
        return (com.communote.server.model.config.ClientConfiguration) this
                .load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.config.ClientConfigurationDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.ClientConfiguration> loadAll() {
        return (java.util.Collection<com.communote.server.model.config.ClientConfiguration>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.config.ClientConfigurationDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.config.ClientConfigurationImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.config.ClientConfigurationDao#remove(com.communote.server.model.config.ClientConfiguration)
     */
    public void remove(com.communote.server.model.config.ClientConfiguration clientConfiguration) {
        if (clientConfiguration == null) {
            throw new IllegalArgumentException(
                    "ClientConfiguration.remove - 'clientConfiguration' can not be null");
        }
        this.getHibernateTemplate().delete(clientConfiguration);
    }

    /**
     * @see 
     *      com.communote.server.persistence.config.ClientConfigurationDao#remove(java.util.Collection
     *      <com.communote.server.persistence.config.ClientConfiguration>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.config.ClientConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ClientConfiguration.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.config.ClientConfigurationDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ClientConfiguration.remove - 'id' can not be null");
        }
        com.communote.server.model.config.ClientConfiguration entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.config.ClientConfiguration)} method.
     * This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.config.ClientConfigurationDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.config.ClientConfiguration)
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
     * <code>com.communote.server.persistence.config.ClientConfigurationDao</code>, please note that
     * the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.config.ClientConfigurationDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.config.ClientConfiguration entity) {
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
     * @see com.communote.server.persistence.config.ClientConfigurationDao#update(com.communote.server.model.config.ClientConfiguration)
     */
    public void update(com.communote.server.model.config.ClientConfiguration clientConfiguration) {
        if (clientConfiguration == null) {
            throw new IllegalArgumentException(
                    "ClientConfiguration.update - 'clientConfiguration' can not be null");
        }
        this.getHibernateTemplate().update(clientConfiguration);
    }

    /**
     * @see 
     *      com.communote.server.persistence.config.ClientConfigurationDao#update(java.util.Collection
     *      <com.communote.server.persistence.config.ClientConfiguration>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.config.ClientConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ClientConfiguration.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.ClientConfiguration>() {
                            public com.communote.server.model.config.ClientConfiguration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.ClientConfiguration> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}