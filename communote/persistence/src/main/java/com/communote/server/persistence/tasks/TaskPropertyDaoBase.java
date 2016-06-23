package com.communote.server.persistence.tasks;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.tasks.TaskProperty</code>.
 * </p>
 * 
 * @see com.communote.server.model.task.TaskProperty
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class TaskPropertyDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.tasks.TaskPropertyDao {

    /**
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#create(com.communote.server.model.task.TaskProperty)
     */
    public com.communote.server.model.task.TaskProperty create(
            com.communote.server.model.task.TaskProperty taskProperty) {
        return (com.communote.server.model.task.TaskProperty) this.create(TRANSFORM_NONE,
                taskProperty);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#create(int transform,
     *      com.communote.server.persistence.tasks.TaskProperty)
     */
    public Object create(final int transform,
            final com.communote.server.model.task.TaskProperty taskProperty) {
        if (taskProperty == null) {
            throw new IllegalArgumentException(
                    "TaskProperty.create - 'taskProperty' can not be null");
        }
        this.getHibernateTemplate().save(taskProperty);
        return this.transformEntity(transform, taskProperty);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.tasks.TaskProperty>)
     */
    public java.util.Collection<com.communote.server.model.task.TaskProperty> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.task.TaskProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("TaskProperty.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.task.TaskProperty>() {
                            public com.communote.server.model.task.TaskProperty doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.task.TaskProperty> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.tasks.TaskProperty>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.task.TaskProperty> create(
            final java.util.Collection<com.communote.server.model.task.TaskProperty> entities) {
        return (java.util.Collection<com.communote.server.model.task.TaskProperty>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.task.TaskProperty entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("TaskProperty.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.task.TaskPropertyImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.task.TaskProperty) entity);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#load(Long)
     */
    public com.communote.server.model.task.TaskProperty load(Long id) {
        return (com.communote.server.model.task.TaskProperty) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.task.TaskProperty> loadAll() {
        return (java.util.Collection<com.communote.server.model.task.TaskProperty>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.task.TaskPropertyImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#remove(com.communote.server.model.task.TaskProperty)
     */
    public void remove(com.communote.server.model.task.TaskProperty taskProperty) {
        if (taskProperty == null) {
            throw new IllegalArgumentException(
                    "TaskProperty.remove - 'taskProperty' can not be null");
        }
        this.getHibernateTemplate().delete(taskProperty);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.tasks.TaskProperty>)
     */
    public void remove(java.util.Collection<com.communote.server.model.task.TaskProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("TaskProperty.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("TaskProperty.remove - 'id' can not be null");
        }
        com.communote.server.model.task.TaskProperty entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.task.TaskProperty)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.tasks.TaskPropertyDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.task.TaskProperty)
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
     * <code>com.communote.server.persistence.tasks.TaskPropertyDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.tasks.TaskPropertyDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.task.TaskProperty entity) {
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
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#update(com.communote.server.model.task.TaskProperty)
     */
    public void update(com.communote.server.model.task.TaskProperty taskProperty) {
        if (taskProperty == null) {
            throw new IllegalArgumentException(
                    "TaskProperty.update - 'taskProperty' can not be null");
        }
        this.getHibernateTemplate().update(taskProperty);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskPropertyDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.tasks.TaskProperty>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.task.TaskProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("TaskProperty.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.task.TaskProperty>() {
                            public com.communote.server.model.task.TaskProperty doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.task.TaskProperty> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}