package com.communote.server.persistence.user.client;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.client.Client</code>.
 * </p>
 *
 * @see com.communote.server.model.client.Client
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ClientDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.client.ClientDao {

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#changeClientStatus(String,
     *      com.communote.server.model.client.ClientStatus,
     *      com.communote.server.model.client.ClientStatus)
     */
    @Override
    public com.communote.server.model.client.Client changeClientStatus(final String clientId,
            final com.communote.server.model.client.ClientStatus newStatus,
            final com.communote.server.model.client.ClientStatus oldStatus) {
        if (clientId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.client.ClientDao.changeClientStatus(String clientId, ClientStatus newStatus, ClientStatus oldStatus) - 'clientId' can not be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.client.ClientDao.changeClientStatus(String clientId, ClientStatus newStatus, ClientStatus oldStatus) - 'newStatus' can not be null");
        }
        if (oldStatus == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.client.ClientDao.changeClientStatus(String clientId, ClientStatus newStatus, ClientStatus oldStatus) - 'oldStatus' can not be null");
        }
        try {
            return this.handleChangeClientStatus(clientId, newStatus, oldStatus);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.client.ClientDao.changeClientStatus(String clientId, ClientStatus newStatus, ClientStatus oldStatus)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#create(com.communote.server.model.client.Client)
     */
    @Override
    public com.communote.server.model.client.Client create(
            com.communote.server.model.client.Client client) {
        return (com.communote.server.model.client.Client) this.create(TRANSFORM_NONE, client);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#create(int transform,
     *      com.communote.server.persistence.user.client.Client)
     */
    @Override
    public Object create(final int transform, final com.communote.server.model.client.Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client.create - 'client' can not be null");
        }
        this.getHibernateTemplate().save(client);
        return this.transformEntity(transform, client);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.client.Client>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.client.Client> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.client.Client> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Client.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.client.Client>() {
                            @Override
                            public com.communote.server.model.client.Client doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.client.Client> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.user.client.Client>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.client.Client> create(
            final java.util.Collection<com.communote.server.model.client.Client> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.client.Client entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#findByClientId(String)
     */
    @Override
    public com.communote.server.model.client.Client findByClientId(final String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.client.ClientDao.findByClientId(String clientId) - 'clientId' can not be null");
        }
        try {
            return this.handleFindByClientId(clientId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.client.ClientDao.findByClientId(String clientId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#findByClientIdForWriting(String)
     */
    @Override
    public com.communote.server.model.client.Client findByClientIdForWriting(final String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.client.ClientDao.findByClientIdForWriting(String clientId) - 'clientId' can not be null");
        }
        try {
            return this.handleFindByClientIdForWriting(clientId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.client.ClientDao.findByClientIdForWriting(String clientId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #changeClientStatus(String, com.communote.server.model.client.ClientStatus, com.communote.server.model.client.ClientStatus)}
     */
    protected abstract com.communote.server.model.client.Client handleChangeClientStatus(
            String clientId, com.communote.server.model.client.ClientStatus newStatus,
            com.communote.server.model.client.ClientStatus oldStatus);

    /**
     * Performs the core logic for {@link #findByClientId(String)}
     */
    protected abstract com.communote.server.model.client.Client handleFindByClientId(String clientId);

    /**
     * Performs the core logic for {@link #findByClientIdForWriting(String)}
     */
    protected abstract com.communote.server.model.client.Client handleFindByClientIdForWriting(
            String clientId);

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Client.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.client.Client.class, id);
        return transformEntity(transform, (com.communote.server.model.client.Client) entity);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#load(Long)
     */
    @Override
    public com.communote.server.model.client.Client load(Long id) {
        return (com.communote.server.model.client.Client) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.client.Client> loadAll() {
        return (java.util.Collection<com.communote.server.model.client.Client>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.client.Client.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#remove(com.communote.server.model.client.Client)
     */
    @Override
    public void remove(com.communote.server.model.client.Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client.remove - 'client' can not be null");
        }
        this.getHibernateTemplate().delete(client);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.user.client.Client>)
     */
    @Override
    public void remove(java.util.Collection<com.communote.server.model.client.Client> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Client.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Client.remove - 'id' can not be null");
        }
        com.communote.server.model.client.Client entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.client.Client)} method. This method
     * does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.client.ClientDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.client.Client)
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
     * <code>com.communote.server.persistence.user.client.ClientDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.client.ClientDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.client.Client entity) {
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
     * @see com.communote.server.persistence.user.client.ClientDao#update(com.communote.server.model.client.Client)
     */
    @Override
    public void update(com.communote.server.model.client.Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client.update - 'client' can not be null");
        }
        this.getHibernateTemplate().update(client);
    }

    /**
     * @see com.communote.server.persistence.user.client.ClientDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.user.client.Client>)
     */
    @Override
    public void update(final java.util.Collection<com.communote.server.model.client.Client> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Client.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.client.Client>() {
                            @Override
                            public com.communote.server.model.client.Client doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.client.Client> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}