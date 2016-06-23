package com.communote.server.persistence.property;

import com.communote.server.model.property.PluginProperty;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>PluginProperty</code>.
 * </p>
 *
 * @see PluginProperty
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class PluginPropertyDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements PluginPropertyDao {

    /**
     * @see PluginPropertyDao#create(int, java.util.Collection<PluginProperty>)
     */
    @Override
    public java.util.Collection<PluginProperty> create(final int transform,
            final java.util.Collection<PluginProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("PluginProperty.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<PluginProperty>() {
                    @Override
                    public PluginProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<PluginProperty> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see PluginPropertyDao#create(int transform, PluginProperty)
     */
    @Override
    public Object create(final int transform, final PluginProperty pluginProperty) {
        if (pluginProperty == null) {
            throw new IllegalArgumentException(
                    "PluginProperty.create - 'pluginProperty' can not be null");
        }
        this.getHibernateTemplate().save(pluginProperty);
        return this.transformEntity(transform, pluginProperty);
    }

    /**
     * @see PluginPropertyDao#create(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.property.PluginProperty>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<PluginProperty> create(
            final java.util.Collection<PluginProperty> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see PluginPropertyDao#create(PluginProperty)
     */
    @Override
    public PluginProperty create(PluginProperty pluginProperty) {
        return (PluginProperty) this.create(TRANSFORM_NONE, pluginProperty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(PluginProperty entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see PluginPropertyDao#find(String, String)
     */
    @Override
    public PluginProperty find(final String symbolicName, final String propertyKey) {
        if (symbolicName == null) {
            throw new IllegalArgumentException(
                    "PluginPropertyDao.find(String symbolicName, String propertyKey) - 'symbolicName' can not be null");
        }
        if (propertyKey == null) {
            throw new IllegalArgumentException(
                    "PluginPropertyDao.find(String symbolicName, String propertyKey) - 'propertyKey' can not be null");
        }
        try {
            return this.handleFind(symbolicName, propertyKey);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'PluginPropertyDao.find(String symbolicName, String propertyKey)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see PluginPropertyDao#getAllProperties(String, boolean)
     */
    @Override
    public java.util.Map<String, String> getAllProperties(final String symbolicName,
            final boolean applicationProperty) {
        if (symbolicName == null) {
            throw new IllegalArgumentException(
                    "PluginPropertyDao.getAllProperties(String symbolicName, boolean applicationProperty) - 'symbolicName' can not be null");
        }
        try {
            return this.handleGetAllProperties(symbolicName, applicationProperty);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'PluginPropertyDao.getAllProperties(String symbolicName, boolean applicationProperty)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #find(String, String)}
     */
    protected abstract PluginProperty handleFind(String symbolicName, String propertyKey);

    /**
     * Performs the core logic for {@link #getAllProperties(String, boolean)}
     */
    protected abstract java.util.Map<String, String> handleGetAllProperties(String symbolicName,
            boolean applicationProperty);

    /**
     * @see PluginPropertyDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("PluginProperty.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(PluginProperty.class, id);
        return transformEntity(transform, (PluginProperty) entity);
    }

    /**
     * @see PluginPropertyDao#load(Long)
     */
    @Override
    public PluginProperty load(Long id) {
        return (PluginProperty) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see PluginPropertyDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<PluginProperty> loadAll() {
        return (java.util.Collection<PluginProperty>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see PluginPropertyDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                PluginProperty.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see PluginPropertyDao#remove(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.property.PluginProperty>)
     */
    @Override
    public void remove(java.util.Collection<PluginProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("PluginProperty.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see PluginPropertyDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("PluginProperty.remove - 'id' can not be null");
        }
        PluginProperty entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see PluginPropertyDao#remove(PluginProperty)
     */
    @Override
    public void remove(PluginProperty pluginProperty) {
        if (pluginProperty == null) {
            throw new IllegalArgumentException(
                    "PluginProperty.remove - 'pluginProperty' can not be null");
        }
        this.getHibernateTemplate().delete(pluginProperty);
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,PluginProperty)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>PluginPropertyDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,PluginProperty)
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
     * <code>PluginPropertyDao</code>, please note that the {@link #TRANSFORM_NONE} constant denotes
     * no transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in {@link PluginPropertyDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final PluginProperty entity) {
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
     * @see PluginPropertyDao#update(java.util.Collection<de
     *      .communardo.kenmei.core.api.bo.property.PluginProperty>)
     */
    @Override
    public void update(final java.util.Collection<PluginProperty> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("PluginProperty.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<PluginProperty>() {
                    @Override
                    public PluginProperty doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<PluginProperty> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

    /**
     * @see PluginPropertyDao#update(PluginProperty)
     */
    @Override
    public void update(PluginProperty pluginProperty) {
        if (pluginProperty == null) {
            throw new IllegalArgumentException(
                    "PluginProperty.update - 'pluginProperty' can not be null");
        }
        this.getHibernateTemplate().update(pluginProperty);
    }

}