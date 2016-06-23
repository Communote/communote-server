package com.communote.server.persistence.config;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.config.LdapSearchConfiguration</code>.
 * </p>
 * 
 * @see com.communote.server.model.config.LdapSearchConfiguration
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class LdapSearchConfigurationDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.config.LdapSearchConfigurationDao {

    /**
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#create(com.communote.server.model.config.LdapSearchConfiguration)
     */
    public com.communote.server.model.config.LdapSearchConfiguration create(
            com.communote.server.model.config.LdapSearchConfiguration ldapSearchConfiguration) {
        return (com.communote.server.model.config.LdapSearchConfiguration) this.create(
                TRANSFORM_NONE, ldapSearchConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#create(int transform,
     *      com.communote.server.persistence.config.LdapSearchConfiguration)
     */
    public Object create(final int transform,
            final com.communote.server.model.config.LdapSearchConfiguration ldapSearchConfiguration) {
        if (ldapSearchConfiguration == null) {
            throw new IllegalArgumentException(
                    "LdapSearchConfiguration.create - 'ldapSearchConfiguration' can not be null");
        }
        this.getHibernateTemplate().save(ldapSearchConfiguration);
        return this.transformEntity(transform, ldapSearchConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.config.LdapSearchConfiguration>)
     */
    public java.util.Collection<com.communote.server.model.config.LdapSearchConfiguration> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.config.LdapSearchConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "LdapSearchConfiguration.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.LdapSearchConfiguration>() {
                            public com.communote.server.model.config.LdapSearchConfiguration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.LdapSearchConfiguration> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#create(java.util.
     *      Collection<com.communote.server.persistence.config.LdapSearchConfiguration>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.LdapSearchConfiguration> create(
            final java.util.Collection<com.communote.server.model.config.LdapSearchConfiguration> entities) {
        return (java.util.Collection<com.communote.server.model.config.LdapSearchConfiguration>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.config.LdapSearchConfiguration entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "LdapSearchConfiguration.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.config.LdapSearchConfigurationImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.config.LdapSearchConfiguration) entity);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#load(Long)
     */
    public com.communote.server.model.config.LdapSearchConfiguration load(Long id) {
        return (com.communote.server.model.config.LdapSearchConfiguration) this.load(
                TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.LdapSearchConfiguration> loadAll() {
        return (java.util.Collection<com.communote.server.model.config.LdapSearchConfiguration>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.config.LdapSearchConfigurationImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#remove(com.communote.server.model.config.LdapSearchConfiguration)
     */
    public void remove(
            com.communote.server.model.config.LdapSearchConfiguration ldapSearchConfiguration) {
        if (ldapSearchConfiguration == null) {
            throw new IllegalArgumentException(
                    "LdapSearchConfiguration.remove - 'ldapSearchConfiguration' can not be null");
        }
        this.getHibernateTemplate().delete(ldapSearchConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#remove(java.util.
     *      Collection<com.communote.server.persistence.config.LdapSearchConfiguration>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.config.LdapSearchConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "LdapSearchConfiguration.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "LdapSearchConfiguration.remove - 'id' can not be null");
        }
        com.communote.server.model.config.LdapSearchConfiguration entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.config.LdapSearchConfiguration)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.config.LdapSearchConfigurationDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.config.LdapSearchConfiguration)
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
     * <code>com.communote.server.persistence.config.LdapSearchConfigurationDao</code>, please note
     * that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself
     * will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.config.LdapSearchConfigurationDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.config.LdapSearchConfiguration entity) {
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
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#update(com.communote.server.model.config.LdapSearchConfiguration)
     */
    public void update(
            com.communote.server.model.config.LdapSearchConfiguration ldapSearchConfiguration) {
        if (ldapSearchConfiguration == null) {
            throw new IllegalArgumentException(
                    "LdapSearchConfiguration.update - 'ldapSearchConfiguration' can not be null");
        }
        this.getHibernateTemplate().update(ldapSearchConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchConfigurationDao#update(java.util.
     *      Collection<com.communote.server.persistence.config.LdapSearchConfiguration>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.config.LdapSearchConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "LdapSearchConfiguration.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.LdapSearchConfiguration>() {
                            public com.communote.server.model.config.LdapSearchConfiguration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.LdapSearchConfiguration> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}