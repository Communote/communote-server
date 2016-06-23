package com.communote.server.persistence.config;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.config.Configuration</code>.
 * </p>
 * 
 * @see com.communote.server.model.config.Configuration
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ConfigurationDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.config.ConfigurationDao {

    /**
     * @see com.communote.server.persistence.config.ConfigurationDao#create(com.communote.server.model.config.Configuration)
     */
    public com.communote.server.model.config.Configuration create(
            com.communote.server.model.config.Configuration configuration) {
        return (com.communote.server.model.config.Configuration) this.create(TRANSFORM_NONE,
                configuration);
    }

    /**
     * @see com.communote.server.persistence.config.ConfigurationDao#create(int transform,
     *      com.communote.server.persistence.config.Configuration)
     */
    public Object create(final int transform,
            final com.communote.server.model.config.Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException(
                    "Configuration.create - 'configuration' can not be null");
        }
        this.getHibernateTemplate().save(configuration);
        return this.transformEntity(transform, configuration);
    }

    /**
     * @see com.communote.server.persistence.config.ConfigurationDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.config.Configuration>)
     */
    public java.util.Collection<com.communote.server.model.config.Configuration> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.config.Configuration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Configuration.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.Configuration>() {
                            public com.communote.server.model.config.Configuration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.Configuration> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.config.ConfigurationDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.config.Configuration>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.Configuration> create(
            final java.util.Collection<com.communote.server.model.config.Configuration> entities) {
        return (java.util.Collection<com.communote.server.model.config.Configuration>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.config.Configuration entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.config.ConfigurationDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Configuration.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.config.ConfigurationImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.config.Configuration) entity);
    }

    /**
     * @see com.communote.server.persistence.config.ConfigurationDao#load(Long)
     */
    public com.communote.server.model.config.Configuration load(Long id) {
        return (com.communote.server.model.config.Configuration) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.config.ConfigurationDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.Configuration> loadAll() {
        return (java.util.Collection<com.communote.server.model.config.Configuration>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.config.ConfigurationDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.config.ConfigurationImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.config.ConfigurationDao#remove(com.communote.server.model.config.Configuration)
     */
    public void remove(com.communote.server.model.config.Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException(
                    "Configuration.remove - 'configuration' can not be null");
        }
        this.getHibernateTemplate().delete(configuration);
    }

    /**
     * @see com.communote.server.persistence.config.ConfigurationDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.config.Configuration>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.config.Configuration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Configuration.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.config.ConfigurationDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Configuration.remove - 'id' can not be null");
        }
        com.communote.server.model.config.Configuration entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.config.Configuration)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.config.ConfigurationDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.config.Configuration)
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
     * <code>com.communote.server.persistence.config.ConfigurationDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.config.ConfigurationDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.config.Configuration entity) {
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
     * @see com.communote.server.persistence.config.ConfigurationDao#update(com.communote.server.model.config.Configuration)
     */
    public void update(com.communote.server.model.config.Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException(
                    "Configuration.update - 'configuration' can not be null");
        }
        this.getHibernateTemplate().update(configuration);
    }

    /**
     * @see com.communote.server.persistence.config.ConfigurationDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.config.Configuration>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.config.Configuration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Configuration.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.Configuration>() {
                            public com.communote.server.model.config.Configuration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.Configuration> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}