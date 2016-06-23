package com.communote.server.persistence.property;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.communote.server.core.vo.IdDateTO;
import com.communote.server.model.property.BinaryProperty;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>BinaryProperty</code>.
 * </p>
 *
 * @see BinaryProperty
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class BinaryPropertyDaoBase extends HibernateDaoSupport implements
        BinaryPropertyDao {

    /**
     * @see BinaryPropertyDao#create(BinaryProperty)
     */
    @Override
    public BinaryProperty create(BinaryProperty binaryProperty) {
        return (BinaryProperty) this.create(TRANSFORM_NONE, binaryProperty);
    }

    /**
     * @see BinaryPropertyDao#create(Collection<de
     *      .communardo.kenmei.core.api.bo.property.BinaryProperty>)
     */
    @Override
    public Collection<BinaryProperty> create(final Collection<BinaryProperty> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see BinaryPropertyDao#create(int transform, BinaryProperty)
     */
    @Override
    public Object create(final int transform, final BinaryProperty binaryProperty) {
        if (binaryProperty == null) {
            throw new IllegalArgumentException(
                    "BinaryProperty.create - 'binaryProperty' can not be null");
        }
        this.getHibernateTemplate().save(binaryProperty);
        return this.transformEntity(transform, binaryProperty);
    }

    /**
     * @see BinaryPropertyDao#create(int, Collection<BinaryProperty>)
     */
    @Override
    public Collection<BinaryProperty> create(final int transform,
            final Collection<BinaryProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("BinaryProperty.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<BinaryProperty>() {
                    @Override
                    public BinaryProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (Iterator<BinaryProperty> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(BinaryProperty entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see BinaryPropertyDao#findIdByKey(String, String)
     */
    @Override
    public IdDateTO findIdByKey(final String keyGroup, final String key) {
        if (keyGroup == null) {
            throw new IllegalArgumentException(
                    "BinaryPropertyDao.findByKey(String keyGroup, String key) - 'keyGroup' can not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException(
                    "BinaryPropertyDao.findByKey(String keyGroup, String key) - 'key' can not be null");
        }
        try {
            return this.handleFindIdByKey(keyGroup, key);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'BinaryPropertyDao.findByKey(String keyGroup, String key)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #findIdByKey(String, String)}
     */
    protected abstract IdDateTO handleFindIdByKey(String keyGroup, String key);

    /**
     * @see BinaryPropertyDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("BinaryProperty.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(BinaryProperty.class, id);
        return transformEntity(transform, (BinaryProperty) entity);
    }

    /**
     * @see BinaryPropertyDao#load(Long)
     */
    @Override
    public BinaryProperty load(Long id) {
        return (BinaryProperty) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see BinaryPropertyDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public Collection<BinaryProperty> loadAll() {
        return (Collection<BinaryProperty>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see BinaryPropertyDao#loadAll(int)
     */
    @Override
    public Collection<?> loadAll(final int transform) {
        final Collection<?> results = this.getHibernateTemplate().loadAll(BinaryProperty.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see BinaryPropertyDao#remove(BinaryProperty)
     */
    @Override
    public void remove(BinaryProperty binaryProperty) {
        if (binaryProperty == null) {
            throw new IllegalArgumentException(
                    "BinaryProperty.remove - 'binaryProperty' can not be null");
        }
        this.getHibernateTemplate().delete(binaryProperty);
    }

    /**
     * @see BinaryPropertyDao#remove(Collection<de
     *      .communardo.kenmei.core.api.bo.property.BinaryProperty>)
     */
    @Override
    public void remove(Collection<BinaryProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("BinaryProperty.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see BinaryPropertyDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("BinaryProperty.remove - 'id' can not be null");
        }
        BinaryProperty entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,BinaryProperty)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>BinaryPropertyDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,BinaryProperty)
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
     * <code>BinaryPropertyDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes
     * no transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link BinaryPropertyDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,Collection)
     */
    protected Object transformEntity(final int transform, final BinaryProperty entity) {
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
     * @see BinaryPropertyDao#update(BinaryProperty)
     */
    @Override
    public void update(BinaryProperty binaryProperty) {
        if (binaryProperty == null) {
            throw new IllegalArgumentException(
                    "BinaryProperty.update - 'binaryProperty' can not be null");
        }
        this.getHibernateTemplate().update(binaryProperty);
    }

    /**
     * @see BinaryPropertyDao#update(Collection<de
     *      .communardo.kenmei.core.api.bo.property.BinaryProperty>)
     */
    @Override
    public void update(final Collection<BinaryProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("BinaryProperty.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<BinaryProperty>() {
                    @Override
                    public BinaryProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (Iterator<BinaryProperty> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

}