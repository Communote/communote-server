package com.communote.server.persistence.config;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.config.ConfluenceConfiguration</code>.
 * </p>
 * 
 * @see com.communote.server.model.config.ConfluenceConfiguration
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ConfluenceConfigurationDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.config.ConfluenceConfigurationDao {

    /**
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#create(com.communote.server.model.config.ConfluenceConfiguration)
     */
    public com.communote.server.model.config.ConfluenceConfiguration create(
            com.communote.server.model.config.ConfluenceConfiguration confluenceConfiguration) {
        return (com.communote.server.model.config.ConfluenceConfiguration) this.create(
                TRANSFORM_NONE, confluenceConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#create(int transform,
     *      com.communote.server.persistence.config.ConfluenceConfiguration)
     */
    public Object create(final int transform,
            final com.communote.server.model.config.ConfluenceConfiguration confluenceConfiguration) {
        if (confluenceConfiguration == null) {
            throw new IllegalArgumentException(
                    "ConfluenceConfiguration.create - 'confluenceConfiguration' can not be null");
        }
        this.getHibernateTemplate().save(confluenceConfiguration);
        return this.transformEntity(transform, confluenceConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.config.ConfluenceConfiguration>)
     */
    public java.util.Collection<com.communote.server.model.config.ConfluenceConfiguration> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.config.ConfluenceConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ConfluenceConfiguration.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.ConfluenceConfiguration>() {
                            public com.communote.server.model.config.ConfluenceConfiguration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.ConfluenceConfiguration> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#create(java.util.
     *      Collection<com.communote.server.persistence.config.ConfluenceConfiguration>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.ConfluenceConfiguration> create(
            final java.util.Collection<com.communote.server.model.config.ConfluenceConfiguration> entities) {
        return (java.util.Collection<com.communote.server.model.config.ConfluenceConfiguration>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.config.ConfluenceConfiguration entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "ConfluenceConfiguration.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.config.ConfluenceConfigurationImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.config.ConfluenceConfiguration) entity);
    }

    /**
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#load(Long)
     */
    public com.communote.server.model.config.ConfluenceConfiguration load(Long id) {
        return (com.communote.server.model.config.ConfluenceConfiguration) this.load(
                TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.ConfluenceConfiguration> loadAll() {
        return (java.util.Collection<com.communote.server.model.config.ConfluenceConfiguration>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.config.ConfluenceConfigurationImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#remove(com.communote.server.model.config.ConfluenceConfiguration)
     */
    public void remove(
            com.communote.server.model.config.ConfluenceConfiguration confluenceConfiguration) {
        if (confluenceConfiguration == null) {
            throw new IllegalArgumentException(
                    "ConfluenceConfiguration.remove - 'confluenceConfiguration' can not be null");
        }
        this.getHibernateTemplate().delete(confluenceConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#remove(java.util.
     *      Collection<com.communote.server.persistence.config.ConfluenceConfiguration>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.config.ConfluenceConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ConfluenceConfiguration.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "ConfluenceConfiguration.remove - 'id' can not be null");
        }
        com.communote.server.model.config.ConfluenceConfiguration entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.config.ConfluenceConfiguration)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.config.ConfluenceConfigurationDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.config.ConfluenceConfiguration)
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
     * <code>com.communote.server.persistence.config.ConfluenceConfigurationDao</code>, please note
     * that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself
     * will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.config.ConfluenceConfigurationDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.config.ConfluenceConfiguration entity) {
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
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#update(com.communote.server.model.config.ConfluenceConfiguration)
     */
    public void update(
            com.communote.server.model.config.ConfluenceConfiguration confluenceConfiguration) {
        if (confluenceConfiguration == null) {
            throw new IllegalArgumentException(
                    "ConfluenceConfiguration.update - 'confluenceConfiguration' can not be null");
        }
        this.getHibernateTemplate().update(confluenceConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.ConfluenceConfigurationDao#update(java.util.
     *      Collection<com.communote.server.persistence.config.ConfluenceConfiguration>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.config.ConfluenceConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ConfluenceConfiguration.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.ConfluenceConfiguration>() {
                            public com.communote.server.model.config.ConfluenceConfiguration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.ConfluenceConfiguration> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}