package com.communote.server.persistence.tasks;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.tasks.Task</code>.
 * </p>
 * 
 * @see com.communote.server.model.task.Task
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class TaskDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.tasks.TaskDao {

    /**
     * @see com.communote.server.persistence.tasks.TaskDao#create(com.communote.server.model.task.Task)
     */
    public com.communote.server.model.task.Task create(com.communote.server.model.task.Task task) {
        return (com.communote.server.model.task.Task) this.create(TRANSFORM_NONE, task);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskDao#create(int transform,
     *      com.communote.server.persistence.tasks.Task)
     */
    public Object create(final int transform, final com.communote.server.model.task.Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task.create - 'task' can not be null");
        }
        this.getHibernateTemplate().save(task);
        return this.transformEntity(transform, task);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.tasks.Task>)
     */
    public java.util.Collection<com.communote.server.model.task.Task> create(final int transform,
            final java.util.Collection<com.communote.server.model.task.Task> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Task.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.task.Task>() {
                            public com.communote.server.model.task.Task doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.task.Task> entityIterator = entities
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
     *      com.communote.server.persistence.tasks.TaskDao#create(java.util.Collection<de.communardo.
     *      kenmei.core.api.bo.tasks.Task>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.task.Task> create(
            final java.util.Collection<com.communote.server.model.task.Task> entities) {
        return (java.util.Collection<com.communote.server.model.task.Task>) create(TRANSFORM_NONE,
                entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.task.Task entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskDao#findNextScheduledTask()
     */
    public com.communote.server.model.task.Task findNextScheduledTask() {
        try {
            return this.handleFindNextScheduledTask();
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.tasks.TaskDao.findNextScheduledTask()' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskDao#findNextScheduledTasks(java.util.Date,
     *      int, java.util.Collection<Long>)
     */
    public java.util.Collection<com.communote.server.model.task.Task> findNextScheduledTasks(
            final java.util.Date upperBound, final int maxTasks,
            final java.util.Collection<Long> taskIdsToExclude) {
        if (taskIdsToExclude == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.tasks.TaskDao.findNextScheduledTasks(java.util.Date upperBound, int maxTasks, java.util.Collection<Long> taskIdsToExclude) - 'taskIdsToExclude' can not be null");
        }
        try {
            return this.handleFindNextScheduledTasks(upperBound, maxTasks, taskIdsToExclude);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.tasks.TaskDao.findNextScheduledTasks(java.util.Date upperBound, int maxTasks, java.util.Collection<Long> taskIdsToExclude)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskDao#findTaskByUniqueName(String)
     */
    public com.communote.server.model.task.Task findTaskByUniqueName(final String uniqueName) {
        if (uniqueName == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.tasks.TaskDao.findTaskByUniqueName(String uniqueName) - 'uniqueName' can not be null");
        }
        try {
            return this.handleFindTaskByUniqueName(uniqueName);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.tasks.TaskDao.findTaskByUniqueName(String uniqueName)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #findNextScheduledTask()}
     */
    protected abstract com.communote.server.model.task.Task handleFindNextScheduledTask();

    /**
     * Performs the core logic for {@link #findNextScheduledTasks(java.util.Date, int,
     * java.util.Collection<Long>)}
     */
    protected abstract java.util.Collection<com.communote.server.model.task.Task> handleFindNextScheduledTasks(
            java.util.Date upperBound, int maxTasks, java.util.Collection<Long> taskIdsToExclude);

    /**
     * Performs the core logic for {@link #findTaskByUniqueName(String)}
     */
    protected abstract com.communote.server.model.task.Task handleFindTaskByUniqueName(
            String uniqueName);

    /**
     * @see com.communote.server.persistence.tasks.TaskDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Task.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.task.TaskImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.task.Task) entity);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskDao#load(Long)
     */
    public com.communote.server.model.task.Task load(Long id) {
        return (com.communote.server.model.task.Task) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.task.Task> loadAll() {
        return (java.util.Collection<com.communote.server.model.task.Task>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.task.TaskImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskDao#remove(com.communote.server.model.task.Task)
     */
    public void remove(com.communote.server.model.task.Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task.remove - 'task' can not be null");
        }
        this.getHibernateTemplate().delete(task);
    }

    /**
     * @see 
     *      com.communote.server.persistence.tasks.TaskDao#remove(java.util.Collection<de.communardo.
     *      kenmei.core.api.bo.tasks.Task>)
     */
    public void remove(java.util.Collection<com.communote.server.model.task.Task> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Task.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Task.remove - 'id' can not be null");
        }
        com.communote.server.model.task.Task entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.task.Task)} method. This method does
     * not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.tasks.TaskDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.task.Task)
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
     * <code>com.communote.server.persistence.tasks.TaskDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.tasks.TaskDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.task.Task entity) {
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
     * @see com.communote.server.persistence.tasks.TaskDao#update(com.communote.server.model.task.Task)
     */
    public void update(com.communote.server.model.task.Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task.update - 'task' can not be null");
        }
        this.getHibernateTemplate().update(task);
    }

    /**
     * @see 
     *      com.communote.server.persistence.tasks.TaskDao#update(java.util.Collection<de.communardo.
     *      kenmei.core.api.bo.tasks.Task>)
     */
    public void update(final java.util.Collection<com.communote.server.model.task.Task> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Task.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.task.Task>() {
                            public com.communote.server.model.task.Task doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.task.Task> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}