package com.communote.server.persistence.config;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.config.LdapConfiguration</code>.
 * </p>
 * 
 * @see com.communote.server.model.config.LdapConfiguration
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class LdapConfigurationDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.config.LdapConfigurationDao {

    /**
     * @see com.communote.server.persistence.config.LdapConfigurationDao#create(com.communote.server.model.config.LdapConfiguration)
     */
    public com.communote.server.model.config.LdapConfiguration create(
            com.communote.server.model.config.LdapConfiguration ldapConfiguration) {
        return (com.communote.server.model.config.LdapConfiguration) this.create(TRANSFORM_NONE,
                ldapConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.LdapConfigurationDao#create(int transform,
     *      com.communote.server.persistence.config.LdapConfiguration)
     */
    public Object create(final int transform,
            final com.communote.server.model.config.LdapConfiguration ldapConfiguration) {
        if (ldapConfiguration == null) {
            throw new IllegalArgumentException(
                    "LdapConfiguration.create - 'ldapConfiguration' can not be null");
        }
        this.getHibernateTemplate().save(ldapConfiguration);
        return this.transformEntity(transform, ldapConfiguration);
    }

    /**
     * @see com.communote.server.persistence.config.LdapConfigurationDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.config.LdapConfiguration>)
     */
    public java.util.Collection<com.communote.server.model.config.LdapConfiguration> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.config.LdapConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "LdapConfiguration.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.LdapConfiguration>() {
                            public com.communote.server.model.config.LdapConfiguration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.LdapConfiguration> entityIterator = entities
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
     *      com.communote.server.persistence.config.LdapConfigurationDao#create(java.util.Collection<
     *      com.communote.server.persistence.config.LdapConfiguration>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.LdapConfiguration> create(
            final java.util.Collection<com.communote.server.model.config.LdapConfiguration> entities) {
        return (java.util.Collection<com.communote.server.model.config.LdapConfiguration>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.config.LdapConfiguration entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.config.LdapConfigurationDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("LdapConfiguration.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.config.LdapConfigurationImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.config.LdapConfiguration) entity);
    }

    /**
     * @see com.communote.server.persistence.config.LdapConfigurationDao#load(Long)
     */
    public com.communote.server.model.config.LdapConfiguration load(Long id) {
        return (com.communote.server.model.config.LdapConfiguration) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.config.LdapConfigurationDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.LdapConfiguration> loadAll() {
        return (java.util.Collection<com.communote.server.model.config.LdapConfiguration>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.config.LdapConfigurationDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.config.LdapConfigurationImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.config.LdapConfigurationDao#remove(com.communote.server.model.config.LdapConfiguration)
     */
    public void remove(com.communote.server.model.config.LdapConfiguration ldapConfiguration) {
        if (ldapConfiguration == null) {
            throw new IllegalArgumentException(
                    "LdapConfiguration.remove - 'ldapConfiguration' can not be null");
        }
        this.getHibernateTemplate().delete(ldapConfiguration);
    }

    /**
     * @see 
     *      com.communote.server.persistence.config.LdapConfigurationDao#remove(java.util.Collection<
     *      com.communote.server.persistence.config.LdapConfiguration>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.config.LdapConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "LdapConfiguration.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.config.LdapConfigurationDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("LdapConfiguration.remove - 'id' can not be null");
        }
        com.communote.server.model.config.LdapConfiguration entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.config.LdapConfiguration)} method.
     * This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.config.LdapConfigurationDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.config.LdapConfiguration)
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
     * <code>com.communote.server.persistence.config.LdapConfigurationDao</code>, please note that
     * the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.config.LdapConfigurationDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.config.LdapConfiguration entity) {
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
     * @see com.communote.server.persistence.config.LdapConfigurationDao#update(com.communote.server.model.config.LdapConfiguration)
     */
    public void update(com.communote.server.model.config.LdapConfiguration ldapConfiguration) {
        if (ldapConfiguration == null) {
            throw new IllegalArgumentException(
                    "LdapConfiguration.update - 'ldapConfiguration' can not be null");
        }
        this.getHibernateTemplate().update(ldapConfiguration);
    }

    /**
     * @see 
     *      com.communote.server.persistence.config.LdapConfigurationDao#update(java.util.Collection<
     *      com.communote.server.persistence.config.LdapConfiguration>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.config.LdapConfiguration> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "LdapConfiguration.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.LdapConfiguration>() {
                            public com.communote.server.model.config.LdapConfiguration doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.LdapConfiguration> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}