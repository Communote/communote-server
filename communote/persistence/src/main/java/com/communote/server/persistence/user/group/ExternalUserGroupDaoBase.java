package com.communote.server.persistence.user.group;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.model.user.group.ExternalUserGroupImpl;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ExternalUserGroup</code>.
 * </p>
 * 
 * @see ExternalUserGroup
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ExternalUserGroupDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        ExternalUserGroupDao {

    /**
     * @see ExternalUserGroupDao#create(Collection <ExternalUserGroup>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public Collection<ExternalUserGroup> create(final Collection<ExternalUserGroup> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see ExternalUserGroupDao#create(ExternalUserGroup)
     */
    @Override
    public ExternalUserGroup create(ExternalUserGroup externalUserGroup) {
        return (ExternalUserGroup) this.create(TRANSFORM_NONE, externalUserGroup);
    }

    /**
     * @see ExternalUserGroupDao#create(int, Collection<ExternalUserGroup>)
     */
    @Override
    public Collection<ExternalUserGroup> create(final int transform,
            final Collection<ExternalUserGroup> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ExternalUserGroup.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<ExternalUserGroup>() {
                    @Override
                    public ExternalUserGroup doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (Iterator<ExternalUserGroup> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see ExternalUserGroupDao#create(int transform, ExternalUserGroup)
     */
    @Override
    public Object create(final int transform, final ExternalUserGroup externalUserGroup) {
        if (externalUserGroup == null) {
            throw new IllegalArgumentException(
                    "ExternalUserGroup.create - 'externalUserGroup' can not be null");
        }
        this.getHibernateTemplate().save(externalUserGroup);
        return this.transformEntity(transform, externalUserGroup);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(ExternalUserGroup entity) {
        this.getHibernateTemplate().evict(entity);
    }

    @Override
    public ExternalUserGroup findByAdditionalProperty(String additionalProperty,
            String externalSystemId) {
        if (additionalProperty == null) {
            throw new IllegalArgumentException("additionalProperty cannot be null");
        }
        if (externalSystemId == null) {
            throw new IllegalArgumentException("externalSystemId cannot be null");
        }
        return handleFindByAdditionalProperty(additionalProperty, externalSystemId);
    }

    /**
     * @see ExternalUserGroupDao#findByExternalId(String, String)
     */
    @Override
    public ExternalUserGroup findByExternalId(final String externalId, final String externalSystemId) {
        if (externalId == null) {
            throw new IllegalArgumentException(
                    "ExternalUserGroupDao.findByExternalId(String externalId, String externalSystemId) - 'externalId' can not be null");
        }
        if (externalSystemId == null) {
            throw new IllegalArgumentException(
                    "ExternalUserGroupDao.findByExternalId(String externalId, String externalSystemId) - 'externalSystemId' can not be null");
        }
        try {
            return this.handleFindByExternalId(externalId, externalSystemId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'ExternalUserGroupDao.findByExternalId(String externalId, String externalSystemId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see ExternalUserGroupDao#findBySystemId(String)
     */
    @Override
    public List<ExternalUserGroup> findBySystemId(final String systemId) {
        if (systemId == null) {
            throw new IllegalArgumentException(
                    "ExternalUserGroupDao.findBySystemId(String systemId) - 'systemId' can not be null");
        }
        try {
            return this.handleFindBySystemId(systemId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'ExternalUserGroupDao.findBySystemId(String systemId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see ExternalUserGroupDao#findLatestBySystemId(String, Long, int)
     */
    @Override
    public List<ExternalUserGroup> findLatestBySystemId(final String externalSystemId,
            final Long startId, final int maxCount) {
        if (externalSystemId == null) {
            throw new IllegalArgumentException(
                    "ExternalUserGroupDao.findLatestBySystemId(String externalSystemId, Long startId, int maxCount) - 'externalSystemId' can not be null");
        }
        if (startId == null) {
            throw new IllegalArgumentException(
                    "ExternalUserGroupDao.findLatestBySystemId(String externalSystemId, Long startId, int maxCount) - 'startId' can not be null");
        }
        try {
            return this.handleFindLatestBySystemId(externalSystemId, startId, maxCount);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'ExternalUserGroupDao.findLatestBySystemId(String externalSystemId, Long startId, int maxCount)' --> "
                            + rt, rt);
        }
    }

    abstract protected ExternalUserGroup handleFindByAdditionalProperty(String additionalProperty,
            String externalSystemId);

    /**
     * Performs the core logic for {@link #findByExternalId(String, String)}
     */
    protected abstract ExternalUserGroup handleFindByExternalId(String externalId,
            String externalSystemId);

    /**
     * Performs the core logic for {@link #findBySystemId(String)}
     */
    protected abstract List<ExternalUserGroup> handleFindBySystemId(String systemId);

    /**
     * Performs the core logic for {@link #findLatestBySystemId(String, Long, int)}
     */
    protected abstract List<ExternalUserGroup> handleFindLatestBySystemId(String externalSystemId,
            Long startId, int maxCount);

    /**
     * @see ExternalUserGroupDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ExternalUserGroup.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(ExternalUserGroupImpl.class, id);
        return transformEntity(transform, (ExternalUserGroup) entity);
    }

    /**
     * @see ExternalUserGroupDao#load(Long)
     */
    @Override
    public ExternalUserGroup load(Long id) {
        return (ExternalUserGroup) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see ExternalUserGroupDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public Collection<ExternalUserGroup> loadAll() {
        return (Collection<ExternalUserGroup>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see ExternalUserGroupDao#loadAll(int)
     */
    @Override
    public Collection<?> loadAll(final int transform) {
        final Collection<?> results = this.getHibernateTemplate().loadAll(
                ExternalUserGroupImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see ExternalUserGroupDao#remove(Collection <ExternalUserGroup>)
     */
    @Override
    public void remove(Collection<ExternalUserGroup> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ExternalUserGroup.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see ExternalUserGroupDao#remove(ExternalUserGroup)
     */
    @Override
    public void remove(ExternalUserGroup externalUserGroup) {
        if (externalUserGroup == null) {
            throw new IllegalArgumentException(
                    "ExternalUserGroup.remove - 'externalUserGroup' can not be null");
        }
        this.getHibernateTemplate().delete(externalUserGroup);
    }

    /**
     * @see ExternalUserGroupDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ExternalUserGroup.remove - 'id' can not be null");
        }
        ExternalUserGroup entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,ExternalUserGroup)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in <code>ExternalUserGroupDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,ExternalUserGroup)
     */
    protected void transformEntities(final int transform, final Collection<?> entities) {
        switch (transform) {
        case TRANSFORM_NONE: // fall-through
        default:
            // do nothing;
        }
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter),
     * when the <code>transform</code> flag is set to one of the constants defined in
     * <code>ExternalUserGroupDao</code>, please note that the {@link #TRANSFORM_NONE} constant
     * denotes no transformation, so the entity itself will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in {@link ExternalUserGroupDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,Collection)
     */
    protected Object transformEntity(final int transform, final ExternalUserGroup entity) {
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
     * @see ExternalUserGroupDao#update(Collection <ExternalUserGroup>)
     */
    @Override
    public void update(final Collection<ExternalUserGroup> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ExternalUserGroup.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<ExternalUserGroup>() {
                    @Override
                    public ExternalUserGroup doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (Iterator<ExternalUserGroup> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

    /**
     * @see ExternalUserGroupDao#update(ExternalUserGroup)
     */
    @Override
    public void update(ExternalUserGroup externalUserGroup) {
        if (externalUserGroup == null) {
            throw new IllegalArgumentException(
                    "ExternalUserGroup.update - 'externalUserGroup' can not be null");
        }
        this.getHibernateTemplate().update(externalUserGroup);
    }

}