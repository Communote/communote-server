package com.communote.server.persistence.blog;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.blog.UserToBlogRoleMapping</code>.
 * </p>
 * 
 * @see com.communote.server.model.blog.UserToBlogRoleMapping
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserToBlogRoleMappingDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.blog.UserToBlogRoleMappingDao {

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#create(com.communote.server.model.blog.UserToBlogRoleMapping)
     */
    public com.communote.server.model.blog.UserToBlogRoleMapping create(
            com.communote.server.model.blog.UserToBlogRoleMapping userToBlogRoleMapping) {
        return (com.communote.server.model.blog.UserToBlogRoleMapping) this.create(TRANSFORM_NONE,
                userToBlogRoleMapping);
    }

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#create(int transform,
     *      com.communote.server.persistence.blog.UserToBlogRoleMapping)
     */
    public Object create(final int transform,
            final com.communote.server.model.blog.UserToBlogRoleMapping userToBlogRoleMapping) {
        if (userToBlogRoleMapping == null) {
            throw new IllegalArgumentException(
                    "UserToBlogRoleMapping.create - 'userToBlogRoleMapping' can not be null");
        }
        this.getHibernateTemplate().save(userToBlogRoleMapping);
        return this.transformEntity(transform, userToBlogRoleMapping);
    }

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.blog.UserToBlogRoleMapping>)
     */
    public java.util.Collection<com.communote.server.model.blog.UserToBlogRoleMapping> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.blog.UserToBlogRoleMapping> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "UserToBlogRoleMapping.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.blog.UserToBlogRoleMapping>() {
                            public com.communote.server.model.blog.UserToBlogRoleMapping doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.blog.UserToBlogRoleMapping> entityIterator = entities
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
     *      com.communote.server.persistence.blog.UserToBlogRoleMappingDao#create(java.util.Collection
     *      <com.communote.server.persistence.blog.UserToBlogRoleMapping>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.blog.UserToBlogRoleMapping> create(
            final java.util.Collection<com.communote.server.model.blog.UserToBlogRoleMapping> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.blog.UserToBlogRoleMapping entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#findMappingsForExternal(Long,
     *      Long, Long, boolean, com.communote.server.model.blog.BlogRole, String)
     */
    public java.util.Collection<com.communote.server.model.blog.UserToBlogRoleMapping> findMappingsForExternal(
            final Long blogId, final Long userId, final Long groupId, final boolean grantedByGroup,
            final com.communote.server.model.blog.BlogRole role, final String externalSystemId) {
        try {
            return this.handleFindMappingsForExternal(blogId, userId, groupId, grantedByGroup,
                    role, externalSystemId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.blog.UserToBlogRoleMappingDao.findMappingsForExternal(Long blogId, Long userId, Long groupId, boolean grantedByGroup, com.communote.server.persistence.blog.BlogRole role, String externalSystemId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#getRoleOfUser(Long, Long)
     */
    public com.communote.server.model.blog.BlogRole getRoleOfUser(final Long blogId,
            final Long userId) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.UserToBlogRoleMappingDao.getRoleOfUser(Long blogId, Long userId) - 'blogId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.UserToBlogRoleMappingDao.getRoleOfUser(Long blogId, Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleGetRoleOfUser(blogId, userId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.blog.UserToBlogRoleMappingDao.getRoleOfUser(Long blogId, Long userId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #findMappingsForExternal(Long, Long, Long, boolean, com.communote.server.model.blog.BlogRole, String)}
     */
    protected abstract java.util.Collection<com.communote.server.model.blog.UserToBlogRoleMapping> handleFindMappingsForExternal(
            Long blogId, Long userId, Long groupId, boolean grantedByGroup,
            com.communote.server.model.blog.BlogRole role, String externalSystemId);

    /**
     * Performs the core logic for {@link #getRoleOfUser(Long, Long)}
     */
    protected abstract com.communote.server.model.blog.BlogRole handleGetRoleOfUser(Long blogId,
            Long userId);

    /**
     * Performs the core logic for {@link #removeAllForBlog(Long)}
     */
    protected abstract void handleRemoveAllForBlog(Long blogId);

    /**
     * Performs the core logic for {@link #removeAllForGroup(Long)}
     */
    protected abstract void handleRemoveAllForGroup(Long groupId);

    /**
     * Performs the core logic for {@link #removeAllForGroupMember(Long, Long)}
     */
    protected abstract void handleRemoveAllForGroupMember(Long userId, Long groupId);

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserToBlogRoleMapping.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.blog.UserToBlogRoleMappingImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.blog.UserToBlogRoleMapping) entity);
    }

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#load(Long)
     */
    public com.communote.server.model.blog.UserToBlogRoleMapping load(Long id) {
        return (com.communote.server.model.blog.UserToBlogRoleMapping) this
                .load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.blog.UserToBlogRoleMapping> loadAll() {
        return (java.util.Collection<com.communote.server.model.blog.UserToBlogRoleMapping>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.blog.UserToBlogRoleMappingImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#remove(com.communote.server.model.blog.UserToBlogRoleMapping)
     */
    public void remove(com.communote.server.model.blog.UserToBlogRoleMapping userToBlogRoleMapping) {
        if (userToBlogRoleMapping == null) {
            throw new IllegalArgumentException(
                    "UserToBlogRoleMapping.remove - 'userToBlogRoleMapping' can not be null");
        }
        this.getHibernateTemplate().delete(userToBlogRoleMapping);
    }

    /**
     * @see 
     *      com.communote.server.persistence.blog.UserToBlogRoleMappingDao#remove(java.util.Collection
     *      <com.communote.server.persistence.blog.UserToBlogRoleMapping>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.blog.UserToBlogRoleMapping> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "UserToBlogRoleMapping.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "UserToBlogRoleMapping.remove - 'id' can not be null");
        }
        com.communote.server.model.blog.UserToBlogRoleMapping entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#removeAllForBlog(Long)
     */
    public void removeAllForBlog(final Long blogId) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.UserToBlogRoleMappingDao.removeAllForBlog(Long blogId) - 'blogId' can not be null");
        }
        try {
            this.handleRemoveAllForBlog(blogId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.blog.UserToBlogRoleMappingDao.removeAllForBlog(Long blogId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#removeAllForGroup(Long)
     */
    public void removeAllForGroup(final Long groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.UserToBlogRoleMappingDao.removeAllForGroup(Long groupId) - 'groupId' can not be null");
        }
        try {
            this.handleRemoveAllForGroup(groupId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.blog.UserToBlogRoleMappingDao.removeAllForGroup(Long groupId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#removeAllForGroupMember(Long,
     *      Long)
     */
    public void removeAllForGroupMember(final Long userId, final Long groupId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.UserToBlogRoleMappingDao.removeAllForGroupMember(Long userId, Long groupId) - 'userId' can not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.blog.UserToBlogRoleMappingDao.removeAllForGroupMember(Long userId, Long groupId) - 'groupId' can not be null");
        }
        try {
            this.handleRemoveAllForGroupMember(userId, groupId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.blog.UserToBlogRoleMappingDao.removeAllForGroupMember(Long userId, Long groupId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.blog.UserToBlogRoleMapping)} method.
     * This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.blog.UserToBlogRoleMappingDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.blog.UserToBlogRoleMapping)
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
     * <code>com.communote.server.persistence.blog.UserToBlogRoleMappingDao</code>, please note that
     * the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.blog.UserToBlogRoleMappingDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.blog.UserToBlogRoleMapping entity) {
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
     * @see com.communote.server.persistence.blog.UserToBlogRoleMappingDao#update(com.communote.server.model.blog.UserToBlogRoleMapping)
     */
    public void update(com.communote.server.model.blog.UserToBlogRoleMapping userToBlogRoleMapping) {
        if (userToBlogRoleMapping == null) {
            throw new IllegalArgumentException(
                    "UserToBlogRoleMapping.update - 'userToBlogRoleMapping' can not be null");
        }
        this.getHibernateTemplate().update(userToBlogRoleMapping);
    }

    /**
     * @see 
     *      com.communote.server.persistence.blog.UserToBlogRoleMappingDao#update(java.util.Collection
     *      <com.communote.server.persistence.blog.UserToBlogRoleMapping>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.blog.UserToBlogRoleMapping> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "UserToBlogRoleMapping.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.blog.UserToBlogRoleMapping>() {
                            public com.communote.server.model.blog.UserToBlogRoleMapping doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.blog.UserToBlogRoleMapping> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}