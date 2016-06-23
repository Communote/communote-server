package com.communote.server.persistence.user.note;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.note.UserNoteEntity</code>.
 * </p>
 * 
 * @see com.communote.server.model.user.note.UserNoteEntity
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserNoteEntityDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.note.UserNoteEntityDao {

    /**
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#create(com.communote.server.model.user.note.UserNoteEntity)
     */
    public com.communote.server.model.user.note.UserNoteEntity create(
            com.communote.server.model.user.note.UserNoteEntity userNoteEntity) {
        return (com.communote.server.model.user.note.UserNoteEntity) this.create(TRANSFORM_NONE,
                userNoteEntity);
    }

    /**
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#create(int transform,
     *      com.communote.server.persistence.user.note.UserNoteEntity)
     */
    public Object create(final int transform,
            final com.communote.server.model.user.note.UserNoteEntity userNoteEntity) {
        if (userNoteEntity == null) {
            throw new IllegalArgumentException(
                    "UserNoteEntity.create - 'userNoteEntity' can not be null");
        }
        this.getHibernateTemplate().save(userNoteEntity);
        return this.transformEntity(transform, userNoteEntity);
    }

    /**
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.note.UserNoteEntity>)
     */
    public java.util.Collection<com.communote.server.model.user.note.UserNoteEntity> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.note.UserNoteEntity> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserNoteEntity.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.note.UserNoteEntity>() {
                            public com.communote.server.model.user.note.UserNoteEntity doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.note.UserNoteEntity> entityIterator = entities
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
     *      com.communote.server.persistence.user.note.UserNoteEntityDao#create(java.util.Collection<
     *      com.communote.server.persistence.user.note.UserNoteEntity>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.note.UserNoteEntity> create(
            final java.util.Collection<com.communote.server.model.user.note.UserNoteEntity> entities) {
        return (java.util.Collection<com.communote.server.model.user.note.UserNoteEntity>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.user.note.UserNoteEntity entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#findByUserIdNoteId(Long,
     *      Long)
     */
    public com.communote.server.model.user.note.UserNoteEntity findByUserIdNoteId(
            final Long userId, final Long noteId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.note.UserNoteEntityDao.findByUserIdNoteId(Long userId, Long noteId) - 'userId' can not be null");
        }
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.note.UserNoteEntityDao.findByUserIdNoteId(Long userId, Long noteId) - 'noteId' can not be null");
        }
        try {
            return this.handleFindByUserIdNoteId(userId, noteId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.note.UserNoteEntityDao.findByUserIdNoteId(Long userId, Long noteId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #findByUserIdNoteId(Long, Long)}
     */
    protected abstract com.communote.server.model.user.note.UserNoteEntity handleFindByUserIdNoteId(
            Long userId, Long noteId);

    /**
     * Performs the core logic for {@link #removeUserNoteEntitiesForNote(Long)}
     */
    protected abstract int handleRemoveUserNoteEntitiesForNote(Long noteId);

    /**
     * Performs the core logic for {@link #removeUserNoteEntitiesForUser(Long)}
     */
    protected abstract int handleRemoveUserNoteEntitiesForUser(Long userid);

    /**
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserNoteEntity.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.note.UserNoteEntityImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.user.note.UserNoteEntity) entity);
    }

    /**
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#load(Long)
     */
    public com.communote.server.model.user.note.UserNoteEntity load(Long id) {
        return (com.communote.server.model.user.note.UserNoteEntity) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.note.UserNoteEntity> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.note.UserNoteEntity>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.note.UserNoteEntityImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#remove(com.communote.server.model.user.note.UserNoteEntity)
     */
    public void remove(com.communote.server.model.user.note.UserNoteEntity userNoteEntity) {
        if (userNoteEntity == null) {
            throw new IllegalArgumentException(
                    "UserNoteEntity.remove - 'userNoteEntity' can not be null");
        }
        this.getHibernateTemplate().delete(userNoteEntity);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.note.UserNoteEntityDao#remove(java.util.Collection<
     *      com.communote.server.persistence.user.note.UserNoteEntity>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.user.note.UserNoteEntity> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserNoteEntity.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("UserNoteEntity.remove - 'id' can not be null");
        }
        com.communote.server.model.user.note.UserNoteEntity entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#removeUserNoteEntitiesForNote(Long)
     */
    public int removeUserNoteEntitiesForNote(final Long noteId) {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.note.UserNoteEntityDao.removeUserNoteEntitiesForNote(Long noteId) - 'noteId' can not be null");
        }
        try {
            return this.handleRemoveUserNoteEntitiesForNote(noteId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.note.UserNoteEntityDao.removeUserNoteEntitiesForNote(Long noteId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#removeUserNoteEntitiesForUser(Long)
     */
    public int removeUserNoteEntitiesForUser(final Long userid) {
        if (userid == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.note.UserNoteEntityDao.removeUserNoteEntitiesForUser(Long userid) - 'userid' can not be null");
        }
        try {
            return this.handleRemoveUserNoteEntitiesForUser(userid);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.note.UserNoteEntityDao.removeUserNoteEntitiesForUser(Long userid)' --> "
                            + rt, rt);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.note.UserNoteEntity)} method.
     * This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.note.UserNoteEntityDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.note.UserNoteEntity)
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
     * <code>com.communote.server.persistence.user.note.UserNoteEntityDao</code>, please note that
     * the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.note.UserNoteEntityDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.note.UserNoteEntity entity) {
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
     * @see com.communote.server.persistence.user.note.UserNoteEntityDao#update(com.communote.server.model.user.note.UserNoteEntity)
     */
    public void update(com.communote.server.model.user.note.UserNoteEntity userNoteEntity) {
        if (userNoteEntity == null) {
            throw new IllegalArgumentException(
                    "UserNoteEntity.update - 'userNoteEntity' can not be null");
        }
        this.getHibernateTemplate().update(userNoteEntity);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.note.UserNoteEntityDao#update(java.util.Collection<
     *      com.communote.server.persistence.user.note.UserNoteEntity>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.user.note.UserNoteEntity> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("UserNoteEntity.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.note.UserNoteEntity>() {
                            public com.communote.server.model.user.note.UserNoteEntity doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.note.UserNoteEntity> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}