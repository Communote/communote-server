package com.communote.server.persistence.global;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.global.GlobalId</code>.
 * </p>
 *
 * @see com.communote.server.model.global.GlobalId
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class GlobalIdDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.global.GlobalIdDao {

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#create(com.communote.server.model.global.GlobalId)
     */
    @Override
    public com.communote.server.model.global.GlobalId create(
            com.communote.server.model.global.GlobalId globalId) {
        return (com.communote.server.model.global.GlobalId) this.create(TRANSFORM_NONE, globalId);
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#create(int transform,
     *      com.communote.server.persistence.global.GlobalId)
     */
    @Override
    public Object create(final int transform,
            final com.communote.server.model.global.GlobalId globalId) {
        if (globalId == null) {
            throw new IllegalArgumentException("GlobalId.create - 'globalId' can not be null");
        }
        this.getHibernateTemplate().save(globalId);
        return this.transformEntity(transform, globalId);
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.global.GlobalId>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.global.GlobalId> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.global.GlobalId> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("GlobalId.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.global.GlobalId>() {
                            @Override
                            public com.communote.server.model.global.GlobalId doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.global.GlobalId> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.global.GlobalId>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.global.GlobalId> create(
            final java.util.Collection<com.communote.server.model.global.GlobalId> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#createGlobalId(com.communote.server.model.attachment.Attachment)
     */
    @Override
    public com.communote.server.model.global.GlobalId createGlobalId(
            final com.communote.server.model.attachment.Attachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.global.GlobalIdDao.createGlobalId(Attachment attachment) - 'attachment' can not be null");
        }
        try {
            return this.handleCreateGlobalId(attachment);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.global.GlobalIdDao.createGlobalId(Attachment attachment)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#createGlobalId(com.communote.server.model.blog.Blog)
     */
    @Override
    public com.communote.server.model.global.GlobalId createGlobalId(
            final com.communote.server.model.blog.Blog blog) {
        if (blog == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.global.GlobalIdDao.createGlobalId(Blog blog) - 'blog' can not be null");
        }
        try {
            return this.handleCreateGlobalId(blog);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.global.GlobalIdDao.createGlobalId(Blog blog)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#createGlobalId(com.communote.server.model.note.Note)
     */
    @Override
    public com.communote.server.model.global.GlobalId createGlobalId(
            final com.communote.server.model.note.Note note) {
        if (note == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.global.GlobalIdDao.createGlobalId(Note note) - 'note' can not be null");
        }
        try {
            return this.handleCreateGlobalId(note);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.global.GlobalIdDao.createGlobalId(Note note)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#createGlobalId(com.communote.server.model.tag.Tag)
     */
    @Override
    public com.communote.server.model.global.GlobalId createGlobalId(
            final com.communote.server.model.tag.Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.global.GlobalIdDao.createGlobalId(Tag tag) - 'tag' can not be null");
        }
        try {
            return this.handleCreateGlobalId(tag);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.global.GlobalIdDao.createGlobalId(Tag tag)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#createGlobalId(com.communote.server.model.user.group.Group)
     */
    @Override
    public com.communote.server.model.global.GlobalId createGlobalId(
            final com.communote.server.model.user.group.Group group) {
        if (group == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.global.GlobalIdDao.createGlobalId(Group group) - 'group' can not be null");
        }
        try {
            return this.handleCreateGlobalId(group);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.global.GlobalIdDao.createGlobalId(Group group)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#createGlobalId(com.communote.server.model.user.User)
     */
    @Override
    public com.communote.server.model.global.GlobalId createGlobalId(
            final com.communote.server.model.user.User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.global.GlobalIdDao.createGlobalId(User user) - 'user' can not be null");
        }
        try {
            return this.handleCreateGlobalId(user);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.global.GlobalIdDao.createGlobalId(User user)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.global.GlobalId entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#findByGlobalIdentifier(String)
     */
    @Override
    public com.communote.server.model.global.GlobalId findByGlobalIdentifier(
            final String globalIdentifier) {
        if (globalIdentifier == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.global.GlobalIdDao.findByGlobalIdentifier(String globalIdentifier) - 'globalIdentifier' can not be null");
        }
        try {
            return this.handleFindByGlobalIdentifier(globalIdentifier);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.global.GlobalIdDao.findByGlobalIdentifier(String globalIdentifier)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#findLatestGlobalId()
     */
    @Override
    public com.communote.server.model.global.GlobalId findLatestGlobalId() {
        try {
            return this.handleFindLatestGlobalId();
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.global.GlobalIdDao.findLatestGlobalId()' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #createGlobalId(com.communote.server.model.attachment.Attachment)}
     */
    protected abstract com.communote.server.model.global.GlobalId handleCreateGlobalId(
            com.communote.server.model.attachment.Attachment attachment);

    /**
     * Performs the core logic for {@link #createGlobalId(com.communote.server.model.blog.Blog)}
     */
    protected abstract com.communote.server.model.global.GlobalId handleCreateGlobalId(
            com.communote.server.model.blog.Blog blog);

    /**
     * Performs the core logic for {@link #createGlobalId(com.communote.server.model.note.Note)}
     */
    protected abstract com.communote.server.model.global.GlobalId handleCreateGlobalId(
            com.communote.server.model.note.Note note);

    /**
     * Performs the core logic for {@link #createGlobalId(com.communote.server.model.tag.Tag)}
     */
    protected abstract com.communote.server.model.global.GlobalId handleCreateGlobalId(
            com.communote.server.model.tag.Tag tag);

    /**
     * Performs the core logic for
     * {@link #createGlobalId(com.communote.server.model.user.group.Group)}
     */
    protected abstract com.communote.server.model.global.GlobalId handleCreateGlobalId(
            com.communote.server.model.user.group.Group group);

    /**
     * Performs the core logic for {@link #createGlobalId(com.communote.server.model.user.User)}
     */
    protected abstract com.communote.server.model.global.GlobalId handleCreateGlobalId(
            com.communote.server.model.user.User user);

    /**
     * Performs the core logic for {@link #findByGlobalIdentifier(String)}
     */
    protected abstract com.communote.server.model.global.GlobalId handleFindByGlobalIdentifier(
            String globalIdentifier);

    /**
     * Performs the core logic for {@link #findLatestGlobalId()}
     */
    protected abstract com.communote.server.model.global.GlobalId handleFindLatestGlobalId();

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("GlobalId.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.global.GlobalIdImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.global.GlobalId) entity);
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#load(Long)
     */
    @Override
    public com.communote.server.model.global.GlobalId load(Long id) {
        return (com.communote.server.model.global.GlobalId) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.global.GlobalId> loadAll() {
        return (java.util.Collection<com.communote.server.model.global.GlobalId>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.global.GlobalIdImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#remove(com.communote.server.model.global.GlobalId)
     */
    @Override
    public void remove(com.communote.server.model.global.GlobalId globalId) {
        if (globalId == null) {
            throw new IllegalArgumentException("GlobalId.remove - 'globalId' can not be null");
        }
        this.getHibernateTemplate().delete(globalId);
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.global.GlobalId>)
     */
    @Override
    public void remove(java.util.Collection<com.communote.server.model.global.GlobalId> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("GlobalId.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("GlobalId.remove - 'id' can not be null");
        }
        com.communote.server.model.global.GlobalId entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.global.GlobalId)} method. This method
     * does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.global.GlobalIdDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.global.GlobalId)
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
     * <code>com.communote.server.persistence.global.GlobalIdDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.global.GlobalIdDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.global.GlobalId entity) {
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
     * @see com.communote.server.persistence.global.GlobalIdDao#update(com.communote.server.model.global.GlobalId)
     */
    @Override
    public void update(com.communote.server.model.global.GlobalId globalId) {
        if (globalId == null) {
            throw new IllegalArgumentException("GlobalId.update - 'globalId' can not be null");
        }
        this.getHibernateTemplate().update(globalId);
    }

    /**
     * @see com.communote.server.persistence.global.GlobalIdDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.global.GlobalId>)
     */
    @Override
    public void update(
            final java.util.Collection<com.communote.server.model.global.GlobalId> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("GlobalId.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.global.GlobalId>() {
                            @Override
                            public com.communote.server.model.global.GlobalId doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.global.GlobalId> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}