package com.communote.server.persistence.blog;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.blog.ExternalBlogMember</code>.
 * </p>
 * 
 * @see com.communote.server.model.blog.ExternalBlogMember
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ExternalBlogMemberDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.blog.ExternalBlogMemberDao {

    /**
     * @see com.communote.server.persistence.blog.ExternalBlogMemberDao#create(com.communote.server.model.blog.ExternalBlogMember)
     */
    public com.communote.server.model.blog.ExternalBlogMember create(
            com.communote.server.model.blog.ExternalBlogMember externalBlogMember) {
        return (com.communote.server.model.blog.ExternalBlogMember) this.create(TRANSFORM_NONE,
                externalBlogMember);
    }

    /**
     * @see com.communote.server.persistence.blog.ExternalBlogMemberDao#create(int transform,
     *      com.communote.server.persistence.blog.ExternalBlogMember)
     */
    public Object create(final int transform,
            final com.communote.server.model.blog.ExternalBlogMember externalBlogMember) {
        if (externalBlogMember == null) {
            throw new IllegalArgumentException(
                    "ExternalBlogMember.create - 'externalBlogMember' can not be null");
        }
        this.getHibernateTemplate().save(externalBlogMember);
        return this.transformEntity(transform, externalBlogMember);
    }

    /**
     * @see com.communote.server.persistence.blog.ExternalBlogMemberDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.blog.ExternalBlogMember>)
     */
    public java.util.Collection<com.communote.server.model.blog.ExternalBlogMember> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.blog.ExternalBlogMember> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ExternalBlogMember.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.blog.ExternalBlogMember>() {
                            public com.communote.server.model.blog.ExternalBlogMember doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.blog.ExternalBlogMember> entityIterator = entities
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
     *      com.communote.server.persistence.blog.ExternalBlogMemberDao#create(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.blog.ExternalBlogMember>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.blog.ExternalBlogMember> create(
            final java.util.Collection<com.communote.server.model.blog.ExternalBlogMember> entities) {
        return (java.util.Collection<com.communote.server.model.blog.ExternalBlogMember>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.blog.ExternalBlogMember entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.blog.ExternalBlogMemberDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ExternalBlogMember.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.blog.ExternalBlogMemberImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.blog.ExternalBlogMember) entity);
    }

    /**
     * @see com.communote.server.persistence.blog.ExternalBlogMemberDao#load(Long)
     */
    public com.communote.server.model.blog.ExternalBlogMember load(Long id) {
        return (com.communote.server.model.blog.ExternalBlogMember) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.blog.ExternalBlogMemberDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.blog.ExternalBlogMember> loadAll() {
        return (java.util.Collection<com.communote.server.model.blog.ExternalBlogMember>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.blog.ExternalBlogMemberDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.blog.ExternalBlogMemberImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.blog.ExternalBlogMemberDao#remove(com.communote.server.model.blog.ExternalBlogMember)
     */
    public void remove(com.communote.server.model.blog.ExternalBlogMember externalBlogMember) {
        if (externalBlogMember == null) {
            throw new IllegalArgumentException(
                    "ExternalBlogMember.remove - 'externalBlogMember' can not be null");
        }
        this.getHibernateTemplate().delete(externalBlogMember);
    }

    /**
     * @see 
     *      com.communote.server.persistence.blog.ExternalBlogMemberDao#remove(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.blog.ExternalBlogMember>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.blog.ExternalBlogMember> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ExternalBlogMember.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.blog.ExternalBlogMemberDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ExternalBlogMember.remove - 'id' can not be null");
        }
        com.communote.server.model.blog.ExternalBlogMember entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.blog.ExternalBlogMember)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.blog.ExternalBlogMemberDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.blog.ExternalBlogMember)
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
     * <code>com.communote.server.persistence.blog.ExternalBlogMemberDao</code>, please note that
     * the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.blog.ExternalBlogMemberDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.blog.ExternalBlogMember entity) {
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
     * @see com.communote.server.persistence.blog.ExternalBlogMemberDao#update(com.communote.server.model.blog.ExternalBlogMember)
     */
    public void update(com.communote.server.model.blog.ExternalBlogMember externalBlogMember) {
        if (externalBlogMember == null) {
            throw new IllegalArgumentException(
                    "ExternalBlogMember.update - 'externalBlogMember' can not be null");
        }
        this.getHibernateTemplate().update(externalBlogMember);
    }

    /**
     * @see 
     *      com.communote.server.persistence.blog.ExternalBlogMemberDao#update(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.blog.ExternalBlogMember>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.blog.ExternalBlogMember> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ExternalBlogMember.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.blog.ExternalBlogMember>() {
                            public com.communote.server.model.blog.ExternalBlogMember doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.blog.ExternalBlogMember> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}