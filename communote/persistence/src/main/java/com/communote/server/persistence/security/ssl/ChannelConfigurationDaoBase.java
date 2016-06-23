package com.communote.server.persistence.security.ssl;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.security.ssl.ChannelConfiguration</code>.
 * </p>
 * 
 * @see com.communote.server.model.security.ChannelConfiguration
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ChannelConfigurationDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.security.ssl.ChannelConfigurationDao {

    /**
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#create(com.communote.server.model.security.ChannelConfiguration)
     */
    public com.communote.server.model.security.ChannelConfiguration create(
            com.communote.server.model.security.ChannelConfiguration channelConfiguration) {
        return (com.communote.server.model.security.ChannelConfiguration) this.create(
                TRANSFORM_NONE, channelConfiguration);
    }

    /**
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#create(int
     *      transform, com.communote.server.persistence.security.ssl.ChannelConfiguration)
     */
    public Object create(final int transform,
            final com.communote.server.model.security.ChannelConfiguration channelConfiguration) {
        if (channelConfiguration == null) {
            throw new IllegalArgumentException(
                    "ChannelConfiguration.create - 'channelConfiguration' can not be null");
        }
        this.getHibernateTemplate().save(channelConfiguration);
        return this.transformEntity(transform, channelConfiguration);
    }

    /**
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#create(int,
     *      java.util
     *      .Collection<com.communote.server.persistence.security.ssl.ChannelConfiguration>)
     */
    public java.util.Collection<com.communote.server.model.security.ChannelConfiguration> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.security.ChannelConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ChannelConfiguration.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.security.ChannelConfiguration>() {
                            public com.communote.server.model.security.ChannelConfiguration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.security.ChannelConfiguration> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#create(java.util.
     *      Collection<com.communote.server.persistence.security.ssl.ChannelConfiguration>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.security.ChannelConfiguration> create(
            final java.util.Collection<com.communote.server.model.security.ChannelConfiguration> entities) {
        return (java.util.Collection<com.communote.server.model.security.ChannelConfiguration>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.security.ChannelConfiguration entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ChannelConfiguration.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.security.ChannelConfigurationImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.security.ChannelConfiguration) entity);
    }

    /**
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#load(Long)
     */
    public com.communote.server.model.security.ChannelConfiguration load(Long id) {
        return (com.communote.server.model.security.ChannelConfiguration) this.load(TRANSFORM_NONE,
                id);
    }

    /**
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.security.ChannelConfiguration> loadAll() {
        return (java.util.Collection<com.communote.server.model.security.ChannelConfiguration>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.security.ChannelConfigurationImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#remove(com.communote.server.model.security.ChannelConfiguration)
     */
    public void remove(com.communote.server.model.security.ChannelConfiguration channelConfiguration) {
        if (channelConfiguration == null) {
            throw new IllegalArgumentException(
                    "ChannelConfiguration.remove - 'channelConfiguration' can not be null");
        }
        this.getHibernateTemplate().delete(channelConfiguration);
    }

    /**
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#remove(java.util.
     *      Collection<com.communote.server.persistence.security.ssl.ChannelConfiguration>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.security.ChannelConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ChannelConfiguration.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ChannelConfiguration.remove - 'id' can not be null");
        }
        com.communote.server.model.security.ChannelConfiguration entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.security.ChannelConfiguration)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.security.ssl.ChannelConfigurationDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.security.ChannelConfiguration)
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
     * <code>com.communote.server.persistence.security.ssl.ChannelConfigurationDao</code>, please
     * note that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity
     * itself will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.security.ssl.ChannelConfigurationDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.security.ChannelConfiguration entity) {
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
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#update(com.communote.server.model.security.ChannelConfiguration)
     */
    public void update(com.communote.server.model.security.ChannelConfiguration channelConfiguration) {
        if (channelConfiguration == null) {
            throw new IllegalArgumentException(
                    "ChannelConfiguration.update - 'channelConfiguration' can not be null");
        }
        this.getHibernateTemplate().update(channelConfiguration);
    }

    /**
     * @see com.communote.server.persistence.security.ssl.ChannelConfigurationDao#update(java.util.
     *      Collection<com.communote.server.persistence.security.ssl.ChannelConfiguration>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.security.ChannelConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ChannelConfiguration.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.security.ChannelConfiguration>() {
                            public com.communote.server.model.security.ChannelConfiguration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.security.ChannelConfiguration> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}