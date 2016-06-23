package com.communote.server.persistence.tasks;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.tasks.TaskExecution</code>.
 * </p>
 * 
 * @see com.communote.server.model.task.TaskExecution
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class TaskExecutionDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.tasks.TaskExecutionDao {

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#create(com.communote.server.model.task.TaskExecution)
     */
    public com.communote.server.model.task.TaskExecution create(
            com.communote.server.model.task.TaskExecution taskExecution) {
        return (com.communote.server.model.task.TaskExecution) this.create(TRANSFORM_NONE,
                taskExecution);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#create(int transform,
     *      com.communote.server.persistence.tasks.TaskExecution)
     */
    public Object create(final int transform,
            final com.communote.server.model.task.TaskExecution taskExecution) {
        if (taskExecution == null) {
            throw new IllegalArgumentException(
                    "TaskExecution.create - 'taskExecution' can not be null");
        }
        this.getHibernateTemplate().save(taskExecution);
        return this.transformEntity(transform, taskExecution);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.tasks.TaskExecution>)
     */
    public java.util.Collection<com.communote.server.model.task.TaskExecution> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.task.TaskExecution> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("TaskExecution.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.task.TaskExecution>() {
                            public com.communote.server.model.task.TaskExecution doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.task.TaskExecution> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#create(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.tasks.TaskExecution>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.task.TaskExecution> create(
            final java.util.Collection<com.communote.server.model.task.TaskExecution> entities) {
        return (java.util.Collection<com.communote.server.model.task.TaskExecution>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.task.TaskExecution entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#findTaskExecution(String)
     */
    public com.communote.server.model.task.TaskExecution findTaskExecution(
            final String uniqueTaskName) {
        if (uniqueTaskName == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.tasks.TaskExecutionDao.findTaskExecution(String uniqueTaskName) - 'uniqueTaskName' can not be null");
        }
        try {
            return this.handleFindTaskExecution(uniqueTaskName);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.tasks.TaskExecutionDao.findTaskExecution(String uniqueTaskName)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#findTaskExecutions(String)
     */
    public java.util.Collection<com.communote.server.model.task.TaskExecution> findTaskExecutions(
            final String instanceName) {
        if (instanceName == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.tasks.TaskExecutionDao.findTaskExecutions(String instanceName) - 'instanceName' can not be null");
        }
        try {
            return this.handleFindTaskExecutions(instanceName);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.tasks.TaskExecutionDao.findTaskExecutions(String instanceName)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #findTaskExecution(String)}
     */
    protected abstract com.communote.server.model.task.TaskExecution handleFindTaskExecution(
            String uniqueTaskName);

    /**
     * Performs the core logic for {@link #findTaskExecutions(String)}
     */
    protected abstract java.util.Collection<com.communote.server.model.task.TaskExecution> handleFindTaskExecutions(
            String instanceName);

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("TaskExecution.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.task.TaskExecutionImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.task.TaskExecution) entity);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#load(Long)
     */
    public com.communote.server.model.task.TaskExecution load(Long id) {
        return (com.communote.server.model.task.TaskExecution) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.task.TaskExecution> loadAll() {
        return (java.util.Collection<com.communote.server.model.task.TaskExecution>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.task.TaskExecutionImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#remove(com.communote.server.model.task.TaskExecution)
     */
    public void remove(com.communote.server.model.task.TaskExecution taskExecution) {
        if (taskExecution == null) {
            throw new IllegalArgumentException(
                    "TaskExecution.remove - 'taskExecution' can not be null");
        }
        this.getHibernateTemplate().delete(taskExecution);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#remove(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.tasks.TaskExecution>)
     */
    public void remove(java.util.Collection<com.communote.server.model.task.TaskExecution> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("TaskExecution.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("TaskExecution.remove - 'id' can not be null");
        }
        com.communote.server.model.task.TaskExecution entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.task.TaskExecution)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.tasks.TaskExecutionDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.task.TaskExecution)
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
     * <code>com.communote.server.persistence.tasks.TaskExecutionDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.tasks.TaskExecutionDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.task.TaskExecution entity) {
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
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#update(com.communote.server.model.task.TaskExecution)
     */
    public void update(com.communote.server.model.task.TaskExecution taskExecution) {
        if (taskExecution == null) {
            throw new IllegalArgumentException(
                    "TaskExecution.update - 'taskExecution' can not be null");
        }
        this.getHibernateTemplate().update(taskExecution);
    }

    /**
     * @see com.communote.server.persistence.tasks.TaskExecutionDao#update(java.util.Collection<de.
     *      communardo.kenmei.core.api.bo.tasks.TaskExecution>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.task.TaskExecution> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("TaskExecution.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.task.TaskExecution>() {
                            public com.communote.server.model.task.TaskExecution doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.task.TaskExecution> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}