package com.communote.server.persistence.user.group;

import java.util.Collection;
import java.util.Iterator;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.communote.server.model.user.group.Group;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>Group</code>.
 * </p>
 *
 * @see Group
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class GroupDaoBase extends HibernateDaoSupport implements GroupDao {

    private com.communote.server.persistence.global.GlobalIdDao globalIdDao;

    /**
     * @see GroupDao#count(String)
     */
    @Override
    public int count(final String filter) {
        try {
            return this.handleCount(filter);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'GroupDao.count(String filter)' --> " + rt, rt);
        }
    }

    /**
     * @see GroupDao#countMembers(long)
     */
    @Override
    public int countMembers(final long groupId) {
        try {
            return this.handleCountMembers(groupId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'GroupDao.countMembers(long groupId)' --> " + rt, rt);
        }
    }

    /**
     * @see GroupDao#create(Collection <Group>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public Collection<Group> create(final Collection<Group> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see GroupDao#create(Group)
     */
    @Override
    public Group create(Group group) {
        return (Group) this.create(TRANSFORM_NONE, group);
    }

    /**
     * @see GroupDao#create(int, Collection<Group>)
     */
    @Override
    public Collection<Group> create(final int transform, final Collection<Group> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Group.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(new HibernateCallback<Group>() {
            @Override
            public Group doInHibernate(Session session) throws HibernateException {
                for (Iterator<Group> entityIterator = entities.iterator(); entityIterator.hasNext();) {
                    create(transform, entityIterator.next());
                }
                return null;
            }
        });
        return entities;
    }

    /**
     * @see GroupDao#create(int transform, Group)
     */
    @Override
    public Object create(final int transform, final Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group.create - 'group' can not be null");
        }
        this.getHibernateTemplate().save(group);
        return this.transformEntity(transform, group);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(Group entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see GroupDao#findByAlias(String)
     */
    @Override
    public Group findByAlias(final String alias) {
        if (alias == null) {
            throw new IllegalArgumentException(
                    "GroupDao.findByAlias(String alias) - 'alias' can not be null");
        }
        try {
            return this.handleFindByAlias(alias);
        } catch (RuntimeException rt) {
            throw new RuntimeException("Error performing 'GroupDao.findByAlias(String alias)' --> "
                    + rt, rt);
        }
    }

    /**
     * Gets the reference to <code>globalIdDao</code>.
     */
    protected com.communote.server.persistence.global.GlobalIdDao getGlobalIdDao() {
        return this.globalIdDao;
    }

    /**
     * @see GroupDao#getGroupsOfUser(Long)
     */
    @Override
    public Collection<Group> getGroupsOfUser(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "GroupDao.getGroupsOfUser(Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleGetGroupsOfUser(userId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'GroupDao.getGroupsOfUser(Long userId)' --> " + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #count(String)}
     */
    protected abstract int handleCount(String filter);

    /**
     * Performs the core logic for {@link #countMembers(long)}
     */
    protected abstract int handleCountMembers(long groupId);

    /**
     * Performs the core logic for {@link #findByAlias(String)}
     */
    protected abstract Group handleFindByAlias(String alias);

    /**
     * Performs the core logic for {@link #getGroupsOfUser(Long)}
     */
    protected abstract Collection<Group> handleGetGroupsOfUser(Long userId);

    /**
     * Performs the core logic for {@link #isEntityMember(Long, Long)}
     */
    protected abstract boolean handleIsEntityMember(Long groupId, Long entityId);

    /**
     * Performs the core logic for {@link #loadAllWithReferences()}
     */
    protected abstract Collection<Group> handleLoadAllWithReferences();

    /**
     * Performs the core logic for {@link #loadWithReferences(int, int, String)}
     */
    protected abstract Collection<Group> handleLoadWithReferences(int offset, int count,
            String nameFilter);

    /**
     * @see GroupDao#isEntityMember(Long, Long)
     */
    @Override
    public boolean isEntityMember(final Long groupId, final Long entityId) {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "GroupDao.isEntityMember(Long groupId, Long entityId) - 'groupId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "GroupDao.isEntityMember(Long groupId, Long entityId) - 'entityId' can not be null");
        }
        try {
            return this.handleIsEntityMember(groupId, entityId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'GroupDao.isEntityMember(Long groupId, Long entityId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see GroupDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Group.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(Group.class, id);
        return transformEntity(transform, (Group) entity);
    }

    /**
     * @see GroupDao#load(Long)
     */
    @Override
    public Group load(Long id) {
        return (Group) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see GroupDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public Collection<Group> loadAll() {
        return (Collection<Group>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see GroupDao#loadAll(int)
     */
    @Override
    public Collection<?> loadAll(final int transform) {
        final Collection<?> results = this.getHibernateTemplate().loadAll(Group.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see GroupDao#loadAllWithReferences()
     */
    @Override
    public Collection<Group> loadAllWithReferences() {
        try {
            return this.handleLoadAllWithReferences();
        } catch (RuntimeException rt) {
            throw new RuntimeException("Error performing 'GroupDao.loadAllWithReferences()' --> "
                    + rt, rt);
        }
    }

    /**
     * @see GroupDao#loadWithReferences(int, int, String)
     */
    @Override
    public Collection<Group> loadWithReferences(final int offset, final int count,
            final String nameFilter) {
        if (nameFilter == null) {
            throw new IllegalArgumentException(
                    "GroupDao.loadWithReferences(int offset, int count, String nameFilter) - 'nameFilter' can not be null");
        }
        try {
            return this.handleLoadWithReferences(offset, count, nameFilter);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'GroupDao.loadWithReferences(int offset, int count, String nameFilter)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see GroupDao#remove(Collection <Group>)
     */
    @Override
    public void remove(Collection<Group> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Group.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see GroupDao#remove(Group)
     */
    @Override
    public void remove(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group.remove - 'group' can not be null");
        }
        this.getHibernateTemplate().delete(group);
    }

    /**
     * @see GroupDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Group.remove - 'id' can not be null");
        }
        Group entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Sets the reference to <code>globalIdDao</code>.
     */
    public void setGlobalIdDao(com.communote.server.persistence.global.GlobalIdDao globalIdDao) {
        this.globalIdDao = globalIdDao;
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,Group)} method.
     * This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>GroupDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,Group)
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
     * <code>GroupDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes no
     * transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link GroupDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,Collection)
     */
    protected Object transformEntity(final int transform, final Group entity) {
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
     * @see GroupDao#update(Collection <Group>)
     */
    @Override
    public void update(final Collection<Group> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Group.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(new HibernateCallback<Group>() {
            @Override
            public Group doInHibernate(Session session) throws HibernateException {
                for (Iterator<Group> entityIterator = entities.iterator(); entityIterator.hasNext();) {
                    update(entityIterator.next());
                }
                return null;
            }
        });
    }

    /**
     * @see GroupDao#update(Group)
     */
    @Override
    public void update(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group.update - 'group' can not be null");
        }
        this.getHibernateTemplate().update(group);
    }

}