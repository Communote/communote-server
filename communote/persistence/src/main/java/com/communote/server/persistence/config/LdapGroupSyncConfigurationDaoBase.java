package com.communote.server.persistence.config;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.config.LdapGroupSyncConfiguration</code>.
 * </p>
 * 
 * @see com.communote.server.model.config.LdapGroupSyncConfiguration
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class LdapGroupSyncConfigurationDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.config.LdapGroupSyncConfigurationDao {

    /**
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#create(com.communote.server.model.config.LdapGroupSyncConfiguration)
     */
    public com.communote.server.model.config.LdapGroupSyncConfiguration create(
            com.communote.server.model.config.LdapGroupSyncConfiguration ldapGroupSyncConfiguration) {
        return (com.communote.server.model.config.LdapGroupSyncConfiguration) this.create(
                TRANSFORM_NONE, ldapGroupSyncConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#create(int
     *      transform, com.communote.server.persistence.config.LdapGroupSyncConfiguration)
     */
    public Object create(
            final int transform,
            final com.communote.server.model.config.LdapGroupSyncConfiguration ldapGroupSyncConfiguration) {
        if (ldapGroupSyncConfiguration == null) {
            throw new IllegalArgumentException(
                    "LdapGroupSyncConfiguration.create - 'ldapGroupSyncConfiguration' can not be null");
        }
        this.getHibernateTemplate().save(ldapGroupSyncConfiguration);
        return this.transformEntity(transform, ldapGroupSyncConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#create(int,
     *      java.util
     *      .Collection<com.communote.server.persistence.config.LdapGroupSyncConfiguration>)
     */
    public java.util.Collection<com.communote.server.model.config.LdapGroupSyncConfiguration> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.config.LdapGroupSyncConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "LdapGroupSyncConfiguration.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.LdapGroupSyncConfiguration>() {
                            public com.communote.server.model.config.LdapGroupSyncConfiguration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.LdapGroupSyncConfiguration> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#create(java.util.
     *      Collection<com.communote.server.persistence.config.LdapGroupSyncConfiguration>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.LdapGroupSyncConfiguration> create(
            final java.util.Collection<com.communote.server.model.config.LdapGroupSyncConfiguration> entities) {
        return (java.util.Collection<com.communote.server.model.config.LdapGroupSyncConfiguration>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.config.LdapGroupSyncConfiguration entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "LdapGroupSyncConfiguration.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.config.LdapGroupSyncConfigurationImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.config.LdapGroupSyncConfiguration) entity);
    }

    /**
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#load(Long)
     */
    public com.communote.server.model.config.LdapGroupSyncConfiguration load(Long id) {
        return (com.communote.server.model.config.LdapGroupSyncConfiguration) this.load(
                TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.LdapGroupSyncConfiguration> loadAll() {
        return (java.util.Collection<com.communote.server.model.config.LdapGroupSyncConfiguration>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.config.LdapGroupSyncConfigurationImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#remove(com.communote.server.model.config.LdapGroupSyncConfiguration)
     */
    public void remove(
            com.communote.server.model.config.LdapGroupSyncConfiguration ldapGroupSyncConfiguration) {
        if (ldapGroupSyncConfiguration == null) {
            throw new IllegalArgumentException(
                    "LdapGroupSyncConfiguration.remove - 'ldapGroupSyncConfiguration' can not be null");
        }
        this.getHibernateTemplate().delete(ldapGroupSyncConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#remove(java.util.
     *      Collection<com.communote.server.persistence.config.LdapGroupSyncConfiguration>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.config.LdapGroupSyncConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "LdapGroupSyncConfiguration.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "LdapGroupSyncConfiguration.remove - 'id' can not be null");
        }
        com.communote.server.model.config.LdapGroupSyncConfiguration entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.config.LdapGroupSyncConfiguration)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.config.LdapGroupSyncConfigurationDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.config.LdapGroupSyncConfiguration)
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
     * <code>com.communote.server.persistence.config.LdapGroupSyncConfigurationDao</code>, please
     * note that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity
     * itself will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.config.LdapGroupSyncConfigurationDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.config.LdapGroupSyncConfiguration entity) {
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
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#update(com.communote.server.model.config.LdapGroupSyncConfiguration)
     */
    public void update(
            com.communote.server.model.config.LdapGroupSyncConfiguration ldapGroupSyncConfiguration) {
        if (ldapGroupSyncConfiguration == null) {
            throw new IllegalArgumentException(
                    "LdapGroupSyncConfiguration.update - 'ldapGroupSyncConfiguration' can not be null");
        }
        this.getHibernateTemplate().update(ldapGroupSyncConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.LdapGroupSyncConfigurationDao#update(java.util.
     *      Collection<com.communote.server.persistence.config.LdapGroupSyncConfiguration>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.config.LdapGroupSyncConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "LdapGroupSyncConfiguration.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.LdapGroupSyncConfiguration>() {
                            public com.communote.server.model.config.LdapGroupSyncConfiguration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.LdapGroupSyncConfiguration> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}