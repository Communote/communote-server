package com.communote.server.persistence.user;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.ExternalUserAuthentication</code>.
 * </p>
 * 
 * @see com.communote.server.model.user.ExternalUserAuthentication
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ExternalUserAuthenticationDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.ExternalUserAuthenticationDao {

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#create(com.communote.server.model.user.ExternalUserAuthentication)
     */
    public com.communote.server.model.user.ExternalUserAuthentication create(
            com.communote.server.model.user.ExternalUserAuthentication externalUserAuthentication) {
        return (com.communote.server.model.user.ExternalUserAuthentication) this.create(
                TRANSFORM_NONE, externalUserAuthentication);
    }

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#create(int
     *      transform, com.communote.server.persistence.user.ExternalUserAuthentication)
     */
    public Object create(
            final int transform,
            final com.communote.server.model.user.ExternalUserAuthentication externalUserAuthentication) {
        if (externalUserAuthentication == null) {
            throw new IllegalArgumentException(
                    "ExternalUserAuthentication.create - 'externalUserAuthentication' can not be null");
        }
        this.getHibernateTemplate().save(externalUserAuthentication);
        return this.transformEntity(transform, externalUserAuthentication);
    }

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.ExternalUserAuthentication>)
     */
    public java.util.Collection<com.communote.server.model.user.ExternalUserAuthentication> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.ExternalUserAuthentication> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ExternalUserAuthentication.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.ExternalUserAuthentication>() {
                            public com.communote.server.model.user.ExternalUserAuthentication doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.ExternalUserAuthentication> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#create(java.util.
     *      Collection<com.communote.server.persistence.user.ExternalUserAuthentication>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.ExternalUserAuthentication> create(
            final java.util.Collection<com.communote.server.model.user.ExternalUserAuthentication> entities) {
        return (java.util.Collection<com.communote.server.model.user.ExternalUserAuthentication>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.user.ExternalUserAuthentication entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#findBySystemId(String)
     */
    public java.util.List<com.communote.server.model.user.ExternalUserAuthentication> findBySystemId(
            final String systemId) {
        if (systemId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.ExternalUserAuthenticationDao.findBySystemId(String systemId) - 'systemId' can not be null");
        }
        try {
            return this.handleFindBySystemId(systemId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.ExternalUserAuthenticationDao.findBySystemId(String systemId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#findUserByPermanentId(String,
     *      String)
     */
    public Long findUserByPermanentId(final String externalSystemId, final String permanentId) {
        if (externalSystemId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.ExternalUserAuthenticationDao.findUserByPermanentId(String externalSystemId, String permanentId) - 'externalSystemId' can not be null");
        }
        if (permanentId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.ExternalUserAuthenticationDao.findUserByPermanentId(String externalSystemId, String permanentId) - 'permanentId' can not be null");
        }
        try {
            return this.handleFindUserByPermanentId(externalSystemId, permanentId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.ExternalUserAuthenticationDao.findUserByPermanentId(String externalSystemId, String permanentId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #findBySystemId(String)}
     */
    protected abstract java.util.List<com.communote.server.model.user.ExternalUserAuthentication> handleFindBySystemId(
            String systemId);

    /**
     * Performs the core logic for {@link #findUserByPermanentId(String, String)}
     */
    protected abstract Long handleFindUserByPermanentId(String externalSystemId, String permanentId);

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "ExternalUserAuthentication.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.ExternalUserAuthenticationImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.user.ExternalUserAuthentication) entity);
    }

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#load(Long)
     */
    public com.communote.server.model.user.ExternalUserAuthentication load(Long id) {
        return (com.communote.server.model.user.ExternalUserAuthentication) this.load(
                TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.ExternalUserAuthentication> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.ExternalUserAuthentication>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.ExternalUserAuthenticationImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#remove(com.communote.server.model.user.ExternalUserAuthentication)
     */
    public void remove(
            com.communote.server.model.user.ExternalUserAuthentication externalUserAuthentication) {
        if (externalUserAuthentication == null) {
            throw new IllegalArgumentException(
                    "ExternalUserAuthentication.remove - 'externalUserAuthentication' can not be null");
        }
        this.getHibernateTemplate().delete(externalUserAuthentication);
    }

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#remove(java.util.
     *      Collection<com.communote.server.persistence.user.ExternalUserAuthentication>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.user.ExternalUserAuthentication> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ExternalUserAuthentication.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "ExternalUserAuthentication.remove - 'id' can not be null");
        }
        com.communote.server.model.user.ExternalUserAuthentication entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.ExternalUserAuthentication)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.ExternalUserAuthenticationDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.ExternalUserAuthentication)
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
     * <code>com.communote.server.persistence.user.ExternalUserAuthenticationDao</code>, please note
     * that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself
     * will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.ExternalUserAuthenticationDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.ExternalUserAuthentication entity) {
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
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#update(com.communote.server.model.user.ExternalUserAuthentication)
     */
    public void update(
            com.communote.server.model.user.ExternalUserAuthentication externalUserAuthentication) {
        if (externalUserAuthentication == null) {
            throw new IllegalArgumentException(
                    "ExternalUserAuthentication.update - 'externalUserAuthentication' can not be null");
        }
        this.getHibernateTemplate().update(externalUserAuthentication);
    }

    /**
     * @see com.communote.server.persistence.user.ExternalUserAuthenticationDao#update(java.util.
     *      Collection<com.communote.server.persistence.user.ExternalUserAuthentication>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.user.ExternalUserAuthentication> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ExternalUserAuthentication.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.ExternalUserAuthentication>() {
                            public com.communote.server.model.user.ExternalUserAuthentication doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.ExternalUserAuthentication> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}