package com.communote.server.persistence.external;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.external.ExternalObject</code>.
 * </p>
 * 
 * @see com.communote.server.model.external.ExternalObject
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ExternalObjectDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.external.ExternalObjectDao {

    /**
     * @see com.communote.server.persistence.external.ExternalObjectDao#create(com.communote.server.model.external.ExternalObject)
     */
    public com.communote.server.model.external.ExternalObject create(
            com.communote.server.model.external.ExternalObject externalObject) {
        return (com.communote.server.model.external.ExternalObject) this.create(TRANSFORM_NONE,
                externalObject);
    }

    /**
     * @see com.communote.server.persistence.external.ExternalObjectDao#create(int transform,
     *      com.communote.server.persistence.external.ExternalObject)
     */
    public Object create(final int transform,
            final com.communote.server.model.external.ExternalObject externalObject) {
        if (externalObject == null) {
            throw new IllegalArgumentException(
                    "ExternalObject.create - 'externalObject' can not be null");
        }
        this.getHibernateTemplate().save(externalObject);
        return this.transformEntity(transform, externalObject);
    }

    /**
     * @see com.communote.server.persistence.external.ExternalObjectDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.external.ExternalObject>)
     */
    public java.util.Collection<com.communote.server.model.external.ExternalObject> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.external.ExternalObject> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("ExternalObject.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.external.ExternalObject>() {
                            public com.communote.server.model.external.ExternalObject doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.external.ExternalObject> entityIterator = entities
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
     *      com.communote.server.persistence.external.ExternalObjectDao#create(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.external.ExternalObject>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.external.ExternalObject> create(
            final java.util.Collection<com.communote.server.model.external.ExternalObject> entities) {
        return (java.util.Collection<com.communote.server.model.external.ExternalObject>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.external.ExternalObject entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.external.ExternalObjectDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ExternalObject.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.external.ExternalObjectImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.external.ExternalObject) entity);
    }

    /**
     * @see com.communote.server.persistence.external.ExternalObjectDao#load(Long)
     */
    public com.communote.server.model.external.ExternalObject load(Long id) {
        return (com.communote.server.model.external.ExternalObject) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.external.ExternalObjectDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.external.ExternalObject> loadAll() {
        return (java.util.Collection<com.communote.server.model.external.ExternalObject>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.external.ExternalObjectDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.external.ExternalObjectImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.external.ExternalObjectDao#remove(com.communote.server.model.external.ExternalObject)
     */
    public void remove(com.communote.server.model.external.ExternalObject externalObject) {
        if (externalObject == null) {
            throw new IllegalArgumentException(
                    "ExternalObject.remove - 'externalObject' can not be null");
        }
        this.getHibernateTemplate().delete(externalObject);
    }

    /**
     * @see 
     *      com.communote.server.persistence.external.ExternalObjectDao#remove(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.external.ExternalObject>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.external.ExternalObject> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("ExternalObject.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.external.ExternalObjectDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ExternalObject.remove - 'id' can not be null");
        }
        com.communote.server.model.external.ExternalObject entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.external.ExternalObject)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.external.ExternalObjectDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.external.ExternalObject)
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
     * <code>com.communote.server.persistence.external.ExternalObjectDao</code>, please note that
     * the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.external.ExternalObjectDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.external.ExternalObject entity) {
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
     * @see com.communote.server.persistence.external.ExternalObjectDao#update(com.communote.server.model.external.ExternalObject)
     */
    public void update(com.communote.server.model.external.ExternalObject externalObject) {
        if (externalObject == null) {
            throw new IllegalArgumentException(
                    "ExternalObject.update - 'externalObject' can not be null");
        }
        this.getHibernateTemplate().update(externalObject);
    }

    /**
     * @see 
     *      com.communote.server.persistence.external.ExternalObjectDao#update(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.external.ExternalObject>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.external.ExternalObject> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("ExternalObject.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.external.ExternalObject>() {
                            public com.communote.server.model.external.ExternalObject doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.external.ExternalObject> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}