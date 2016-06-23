package com.communote.server.persistence.tag;

import java.util.Collection;

import com.communote.server.model.tag.Tag;
import com.communote.server.model.tag.TagImpl;

/**
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>Tag</code>.
 * 
 * @see Tag
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class TagDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements TagDao {

    private com.communote.server.persistence.global.GlobalIdDao globalIdDao;

    /**
     * @see TagDao#create(Collection<de.communardo.kenmei .core.api.bo.tag.Tag>)
     */
    @Override
    public Collection<Tag> create(final Collection<Tag> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see TagDao#create(int, Collection<Tag>)
     */
    @Override
    public Collection<Tag> create(final int transform, final Collection<Tag> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Tag.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Tag>() {
                    @Override
                    public Tag doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (Tag entity : entities) {
                            create(transform, entity);
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see TagDao#create(int transform, Tag)
     */
    @Override
    public Object create(final int transform, final Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag.create - 'tag' can not be null");
        }
        this.getHibernateTemplate().save(tag);
        return this.transformEntity(transform, tag);
    }

    /**
     * @see TagDao#create(Tag)
     */
    @Override
    public Tag create(Tag tag) {
        return (Tag) this.create(TRANSFORM_NONE, tag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(Tag entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see TagDao#findByPrefix(String, com.communote.server.core.filter.ResultSpecification)
     */
    @Override
    public java.util.List<com.communote.server.api.core.tag.TagData> findByPrefix(
            final String prefix,
            final com.communote.server.core.filter.ResultSpecification resultSpecification) {
        if (prefix == null) {
            throw new IllegalArgumentException(
                    "TagDao.findByPrefix(String prefix, ResultSpecification resultSpecification) - 'prefix' can not be null");
        }
        if (resultSpecification == null) {
            throw new IllegalArgumentException(
                    "TagDao.findByPrefix(String prefix, ResultSpecification resultSpecification) - 'resultSpecification' can not be null");
        }
        try {
            return this.handleFindByPrefix(prefix, resultSpecification);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'TagDao.findByPrefix(String prefix, ResultSpecification resultSpecification)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see TagDao#findByTagStore(String, String)
     */
    @Override
    public Tag findByTagStore(final String tagStoreTagId, final String tagStoreAlias) {
        if (tagStoreTagId == null) {
            throw new IllegalArgumentException(
                    "TagDao.findByTagStore(String tagStoreTagId, String tagStoreAlias) - 'tagStoreTagId' can not be null");
        }
        if (tagStoreAlias == null) {
            throw new IllegalArgumentException(
                    "TagDao.findByTagStore(String tagStoreTagId, String tagStoreAlias) - 'tagStoreAlias' can not be null");
        }
        try {
            return this.handleFindByTagStore(tagStoreTagId, tagStoreAlias);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'TagDao.findByTagStore(String tagStoreTagId, String tagStoreAlias)' --> "
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
     * Performs the core logic for
     * {@link #findByPrefix(String, com.communote.server.core.filter.ResultSpecification)}
     */
    protected abstract java.util.List<com.communote.server.api.core.tag.TagData> handleFindByPrefix(
            String prefix, com.communote.server.core.filter.ResultSpecification resultSpecification);

    /**
     * Performs the core logic for {@link #findByTagStore(String, String)}
     */
    protected abstract Tag handleFindByTagStore(String tagStoreTagId, String tagStoreAlias);

    /**
     * @see TagDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Tag.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(TagImpl.class, id);
        return transformEntity(transform, (Tag) entity);
    }

    /**
     * @see TagDao#load(Long)
     */
    @Override
    public Tag load(Long id) {
        return (Tag) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see TagDao#loadAll()
     */
    @Override
    public Collection<TagImpl> loadAll() {
        return this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see TagDao#loadAll(int)
     */
    @Override
    public Collection<TagImpl> loadAll(final int transform) {
        final Collection<TagImpl> results = this.getHibernateTemplate().loadAll(TagImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see TagDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Tag.remove - 'id' can not be null");
        }
        Tag entity = this.load(id);
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
     * Transforms a collection of entities using the {@link #transformEntity(int,Tag)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in <code>TagDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,Tag)
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
     * <code>TagDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes no
     * transformation, so the entity itself will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in {@link TagDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,Collection)
     */
    protected Object transformEntity(final int transform, final Tag entity) {
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
     * @see TagDao#update(Collection<de.communardo.kenmei .core.api.bo.tag.Tag>)
     */
    @Override
    public void update(final Collection<Tag> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Tag.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Tag>() {
                    @Override
                    public Tag doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (Tag entity : entities) {
                            update(entity);
                        }
                        return null;
                    }
                });
    }

    /**
     * @see TagDao#update(Tag)
     */
    @Override
    public void update(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag.update - 'tag' can not be null");
        }
        this.getHibernateTemplate().update(tag);
    }
}