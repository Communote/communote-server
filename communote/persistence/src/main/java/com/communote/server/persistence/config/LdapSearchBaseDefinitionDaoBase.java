package com.communote.server.persistence.config;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.config.LdapSearchBaseDefinition</code>.
 * </p>
 * 
 * @see com.communote.server.model.config.LdapSearchBaseDefinition
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class LdapSearchBaseDefinitionDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.config.LdapSearchBaseDefinitionDao {

    /**
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#create(com.communote.server.model.config.LdapSearchBaseDefinition)
     */
    public com.communote.server.model.config.LdapSearchBaseDefinition create(
            com.communote.server.model.config.LdapSearchBaseDefinition ldapSearchBaseDefinition) {
        return (com.communote.server.model.config.LdapSearchBaseDefinition) this.create(
                TRANSFORM_NONE, ldapSearchBaseDefinition);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#create(int
     *      transform, com.communote.server.persistence.config.LdapSearchBaseDefinition)
     */
    public Object create(
            final int transform,
            final com.communote.server.model.config.LdapSearchBaseDefinition ldapSearchBaseDefinition) {
        if (ldapSearchBaseDefinition == null) {
            throw new IllegalArgumentException(
                    "LdapSearchBaseDefinition.create - 'ldapSearchBaseDefinition' can not be null");
        }
        this.getHibernateTemplate().save(ldapSearchBaseDefinition);
        return this.transformEntity(transform, ldapSearchBaseDefinition);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.config.LdapSearchBaseDefinition>)
     */
    public java.util.Collection<com.communote.server.model.config.LdapSearchBaseDefinition> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.config.LdapSearchBaseDefinition> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "LdapSearchBaseDefinition.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.LdapSearchBaseDefinition>() {
                            public com.communote.server.model.config.LdapSearchBaseDefinition doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.LdapSearchBaseDefinition> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#create(java.util.
     *      Collection<com.communote.server.persistence.config.LdapSearchBaseDefinition>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.LdapSearchBaseDefinition> create(
            final java.util.Collection<com.communote.server.model.config.LdapSearchBaseDefinition> entities) {
        return (java.util.Collection<com.communote.server.model.config.LdapSearchBaseDefinition>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.config.LdapSearchBaseDefinition entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "LdapSearchBaseDefinition.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.config.LdapSearchBaseDefinitionImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.config.LdapSearchBaseDefinition) entity);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#load(Long)
     */
    public com.communote.server.model.config.LdapSearchBaseDefinition load(Long id) {
        return (com.communote.server.model.config.LdapSearchBaseDefinition) this.load(
                TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.LdapSearchBaseDefinition> loadAll() {
        return (java.util.Collection<com.communote.server.model.config.LdapSearchBaseDefinition>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.config.LdapSearchBaseDefinitionImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#remove(com.communote.server.model.config.LdapSearchBaseDefinition)
     */
    public void remove(
            com.communote.server.model.config.LdapSearchBaseDefinition ldapSearchBaseDefinition) {
        if (ldapSearchBaseDefinition == null) {
            throw new IllegalArgumentException(
                    "LdapSearchBaseDefinition.remove - 'ldapSearchBaseDefinition' can not be null");
        }
        this.getHibernateTemplate().delete(ldapSearchBaseDefinition);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#remove(java.util.
     *      Collection<com.communote.server.persistence.config.LdapSearchBaseDefinition>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.config.LdapSearchBaseDefinition> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "LdapSearchBaseDefinition.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "LdapSearchBaseDefinition.remove - 'id' can not be null");
        }
        com.communote.server.model.config.LdapSearchBaseDefinition entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.config.LdapSearchBaseDefinition)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.config.LdapSearchBaseDefinitionDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.config.LdapSearchBaseDefinition)
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
     * <code>com.communote.server.persistence.config.LdapSearchBaseDefinitionDao</code>, please note
     * that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself
     * will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.config.LdapSearchBaseDefinitionDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.config.LdapSearchBaseDefinition entity) {
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
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#update(com.communote.server.model.config.LdapSearchBaseDefinition)
     */
    public void update(
            com.communote.server.model.config.LdapSearchBaseDefinition ldapSearchBaseDefinition) {
        if (ldapSearchBaseDefinition == null) {
            throw new IllegalArgumentException(
                    "LdapSearchBaseDefinition.update - 'ldapSearchBaseDefinition' can not be null");
        }
        this.getHibernateTemplate().update(ldapSearchBaseDefinition);
    }

    /**
     * @see com.communote.server.persistence.config.LdapSearchBaseDefinitionDao#update(java.util.
     *      Collection<com.communote.server.persistence.config.LdapSearchBaseDefinition>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.config.LdapSearchBaseDefinition> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "LdapSearchBaseDefinition.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.LdapSearchBaseDefinition>() {
                            public com.communote.server.model.config.LdapSearchBaseDefinition doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.LdapSearchBaseDefinition> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}