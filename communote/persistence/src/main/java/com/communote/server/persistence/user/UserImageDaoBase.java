package com.communote.server.persistence.user;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.UserImage</code>.
 * </p>
 * 
 * @see com.communote.server.model.user.UserImage
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserImageDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.UserImageDao {

    /**
     * @see com.communote.server.persistence.user.UserImageDao#create(com.communote.server.model.user.UserImage)
     */
    public com.communote.server.model.user.UserImage create(
            com.communote.server.model.user.UserImage userImage) {
        return (com.communote.server.model.user.UserImage) this.create(TRANSFORM_NONE, userImage);
    }

    /**
     * @see com.communote.server.persistence.user.UserImageDao#create(int transform,
     *      com.communote.server.persistence.user.UserImage)
     */
    public Object create(final int transform,
            final com.communote.server.model.user.UserImage userImage) {
        if (userImage == null) {
            throw new IllegalArgumentException("UserImage.create - 'userImage' can not be null");
        }
        this.getHibernateTemplate().save(userImage);
        return this.transformEntity(transform, userImage);
    }

    /**
     * @see com.communote.server.persistence.user.UserImageDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.UserImage>)
     */
    public java.util.Collection<com.communote.server.model.user.UserImage> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.UserImage> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserImage.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.UserImage>() {
                            public com.communote.server.model.user.UserImage doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.UserImage> entityIterator = entities
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
     *      com.communote.server.persistence.user.UserImageDao#create(java.util.Collection<de.communardo
     *      .kenmei.core.api.bo.user.UserImage>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.UserImage> create(
            final java.util.Collection<com.communote.server.model.user.UserImage> entities) {
        return (java.util.Collection<com.communote.server.model.user.UserImage>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.user.UserImage entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.UserImageDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserImage.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.UserImageImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.user.UserImage) entity);
    }

    /**
     * @see com.communote.server.persistence.user.UserImageDao#load(Long)
     */
    public com.communote.server.model.user.UserImage load(Long id) {
        return (com.communote.server.model.user.UserImage) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.UserImageDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.UserImage> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.UserImage>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.UserImageDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.UserImageImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.UserImageDao#remove(com.communote.server.model.user.UserImage)
     */
    public void remove(com.communote.server.model.user.UserImage userImage) {
        if (userImage == null) {
            throw new IllegalArgumentException("UserImage.remove - 'userImage' can not be null");
        }
        this.getHibernateTemplate().delete(userImage);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.UserImageDao#remove(java.util.Collection<de.communardo
     *      .kenmei.core.api.bo.user.UserImage>)
     */
    public void remove(java.util.Collection<com.communote.server.model.user.UserImage> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserImage.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.UserImageDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserImage.remove - 'id' can not be null");
        }
        com.communote.server.model.user.UserImage entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.UserImage)} method. This method
     * does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.UserImageDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.UserImage)
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
     * <code>com.communote.server.persistence.user.UserImageDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.UserImageDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.UserImage entity) {
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
     * @see com.communote.server.persistence.user.UserImageDao#update(com.communote.server.model.user.UserImage)
     */
    public void update(com.communote.server.model.user.UserImage userImage) {
        if (userImage == null) {
            throw new IllegalArgumentException("UserImage.update - 'userImage' can not be null");
        }
        this.getHibernateTemplate().update(userImage);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.UserImageDao#update(java.util.Collection<de.communardo
     *      .kenmei.core.api.bo.user.UserImage>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.user.UserImage> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserImage.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.UserImage>() {
                            public com.communote.server.model.user.UserImage doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.UserImage> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}