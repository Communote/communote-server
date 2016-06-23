package com.communote.server.persistence.blog;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.blog.BlogMember</code>.
 * </p>
 * 
 * @see com.communote.server.model.blog.BlogMember
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class BlogMemberDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.blog.BlogMemberDao {

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#create(com.communote.server.model.blog.BlogMember)
     */
    public com.communote.server.model.blog.BlogMember create(
            com.communote.server.model.blog.BlogMember blogMember) {
        return (com.communote.server.model.blog.BlogMember) this.create(TRANSFORM_NONE, blogMember);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#create(int transform,
     *      com.communote.server.persistence.blog.BlogMember)
     */
    public Object create(final int transform,
            final com.communote.server.model.blog.BlogMember blogMember) {
        if (blogMember == null) {
            throw new IllegalArgumentException("BlogMember.create - 'blogMember' can not be null");
        }
        this.getHibernateTemplate().save(blogMember);
        return this.transformEntity(transform, blogMember);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.blog.BlogMember>)
     */
    public java.util.Collection<com.communote.server.model.blog.BlogMember> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.blog.BlogMember> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("BlogMember.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.blog.BlogMember>() {
                            public com.communote.server.model.blog.BlogMember doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.blog.BlogMember> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.blog.BlogMember>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.blog.BlogMember> create(
            final java.util.Collection<com.communote.server.model.blog.BlogMember> entities) {
        return (java.util.Collection<com.communote.server.model.blog.BlogMember>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.blog.BlogMember entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#findByBlogAndEntity(Long, Long)
     */
    public java.util.List<com.communote.server.model.blog.BlogMember> findByBlogAndEntity(
            final Long blogId, final Long entityId) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.BlogMemberDao.findByBlogAndEntity(Long blogId, Long entityId) - 'blogId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.BlogMemberDao.findByBlogAndEntity(Long blogId, Long entityId) - 'entityId' can not be null");
        }
        try {
            return this.handleFindByBlogAndEntity(blogId, entityId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.blog.BlogMemberDao.findByBlogAndEntity(Long blogId, Long entityId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#findByEntity(Long)
     */
    public java.util.Collection<com.communote.server.model.blog.BlogMember> findByEntity(
            final Long entityId) {
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.BlogMemberDao.findByEntity(Long entityId) - 'entityId' can not be null");
        }
        try {
            return this.handleFindByEntity(entityId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.blog.BlogMemberDao.findByEntity(Long entityId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#getBlogRoles(Long, String)
     */
    public java.util.Map<Long, com.communote.server.model.blog.BlogRole> getBlogRoles(
            final Long blogId, final String externalSystemId) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.BlogMemberDao.getBlogRoles(Long blogId, String externalSystemId) - 'blogId' can not be null");
        }
        if (externalSystemId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.BlogMemberDao.getBlogRoles(Long blogId, String externalSystemId) - 'externalSystemId' can not be null");
        }
        try {
            return this.handleGetBlogRoles(blogId, externalSystemId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.blog.BlogMemberDao.getBlogRoles(Long blogId, String externalSystemId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #findByBlogAndEntity(Long, Long)}
     */
    protected abstract java.util.List<com.communote.server.model.blog.BlogMember> handleFindByBlogAndEntity(
            Long blogId, Long entityId);

    /**
     * Performs the core logic for {@link #findByEntity(Long)}
     */
    protected abstract java.util.Collection<com.communote.server.model.blog.BlogMember> handleFindByEntity(
            Long entityId);

    /**
     * Performs the core logic for {@link #getBlogRoles(Long, String)}
     */
    protected abstract java.util.Map<Long, com.communote.server.model.blog.BlogRole> handleGetBlogRoles(
            Long blogId, String externalSystemId);

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("BlogMember.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.blog.BlogMemberImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.blog.BlogMember) entity);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#load(Long)
     */
    public com.communote.server.model.blog.BlogMember load(Long id) {
        return (com.communote.server.model.blog.BlogMember) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.blog.BlogMember> loadAll() {
        return (java.util.Collection<com.communote.server.model.blog.BlogMember>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.blog.BlogMemberImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#remove(com.communote.server.model.blog.BlogMember)
     */
    public void remove(com.communote.server.model.blog.BlogMember blogMember) {
        if (blogMember == null) {
            throw new IllegalArgumentException("BlogMember.remove - 'blogMember' can not be null");
        }
        this.getHibernateTemplate().delete(blogMember);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.blog.BlogMember>)
     */
    public void remove(java.util.Collection<com.communote.server.model.blog.BlogMember> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("BlogMember.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("BlogMember.remove - 'id' can not be null");
        }
        com.communote.server.model.blog.BlogMember entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.blog.BlogMember)} method. This method
     * does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.blog.BlogMemberDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.blog.BlogMember)
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
     * <code>com.communote.server.persistence.blog.BlogMemberDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.blog.BlogMemberDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.blog.BlogMember entity) {
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
     * @see com.communote.server.persistence.blog.BlogMemberDao#update(com.communote.server.model.blog.BlogMember)
     */
    public void update(com.communote.server.model.blog.BlogMember blogMember) {
        if (blogMember == null) {
            throw new IllegalArgumentException("BlogMember.update - 'blogMember' can not be null");
        }
        this.getHibernateTemplate().update(blogMember);
    }

    /**
     * @see com.communote.server.persistence.blog.BlogMemberDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.blog.BlogMember>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.blog.BlogMember> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("BlogMember.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.blog.BlogMember>() {
                            public com.communote.server.model.blog.BlogMember doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.blog.BlogMember> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}