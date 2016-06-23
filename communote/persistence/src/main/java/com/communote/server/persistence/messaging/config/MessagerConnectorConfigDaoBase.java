package com.communote.server.persistence.messaging.config;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.communote.server.model.messaging.MessagerConnectorConfig;
import com.communote.server.model.messaging.MessagerConnectorConfigImpl;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>MessagerConnectorConfig</code>.
 * </p>
 *
 * @see MessagerConnectorConfig
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class MessagerConnectorConfigDaoBase extends HibernateDaoSupport implements
MessagerConnectorConfigDao {

    @Override
    public java.util.Collection<MessagerConnectorConfig> create(final int transform,
            final java.util.Collection<MessagerConnectorConfig> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "MessagerConnectorConfig.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<MessagerConnectorConfig>() {
                            @Override
                            public MessagerConnectorConfig doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<MessagerConnectorConfig> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    @Override
    public Object create(final int transform, final MessagerConnectorConfig messagerConnectorConfig) {
        if (messagerConnectorConfig == null) {
            throw new IllegalArgumentException(
                    "MessagerConnectorConfig.create - 'messagerConnectorConfig' can not be null");
        }
        this.getHibernateTemplate().save(messagerConnectorConfig);
        return this.transformEntity(transform, messagerConnectorConfig);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<MessagerConnectorConfig> create(
            final java.util.Collection<MessagerConnectorConfig> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see com.communote.server.persistence.messaging.config.MessagerConnectorConfigDao#create(MessagerConnectorConfig)
     */
    @Override
    public MessagerConnectorConfig create(MessagerConnectorConfig messagerConnectorConfig) {
        return (MessagerConnectorConfig) this.create(TRANSFORM_NONE, messagerConnectorConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(MessagerConnectorConfig entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.messaging.config.MessagerConnectorConfigDao#load(int,
     *      Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "MessagerConnectorConfig.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate()
                .get(MessagerConnectorConfigImpl.class, id);
        return transformEntity(transform, (MessagerConnectorConfig) entity);
    }

    /**
     * @see com.communote.server.persistence.messaging.config.MessagerConnectorConfigDao#load(Long)
     */
    @Override
    public MessagerConnectorConfig load(Long id) {
        return (MessagerConnectorConfig) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.messaging.config.MessagerConnectorConfigDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<MessagerConnectorConfig> loadAll() {
        return (java.util.Collection<MessagerConnectorConfig>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.messaging.config.MessagerConnectorConfigDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                MessagerConnectorConfigImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    @Override
    public void remove(java.util.Collection<MessagerConnectorConfig> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "MessagerConnectorConfig.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "MessagerConnectorConfig.remove - 'id' can not be null");
        }
        MessagerConnectorConfig entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see com.communote.server.persistence.messaging.config.MessagerConnectorConfigDao#remove(MessagerConnectorConfig)
     */
    @Override
    public void remove(MessagerConnectorConfig messagerConnectorConfig) {
        if (messagerConnectorConfig == null) {
            throw new IllegalArgumentException(
                    "MessagerConnectorConfig.remove - 'messagerConnectorConfig' can not be null");
        }
        this.getHibernateTemplate().delete(messagerConnectorConfig);
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,MessagerConnectorConfig)} method. This method does not
     * instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in <code>MessagerConnectorConfigDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,MessagerConnectorConfig)
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
     * <code>MessagerConnectorConfigDao</code>, please note that the {@link #TRANSFORM_NONE}
     * constant denotes no transformation, so the entity itself will be returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.messaging.config.MessagerConnectorConfigDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final MessagerConnectorConfig entity) {
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

    @Override
    public void update(final java.util.Collection<MessagerConnectorConfig> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "MessagerConnectorConfig.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<MessagerConnectorConfig>() {
                            @Override
                            public MessagerConnectorConfig doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<MessagerConnectorConfig> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

    /**
     * @see com.communote.server.persistence.messaging.config.MessagerConnectorConfigDao#update(MessagerConnectorConfig)
     */
    @Override
    public void update(MessagerConnectorConfig messagerConnectorConfig) {
        if (messagerConnectorConfig == null) {
            throw new IllegalArgumentException(
                    "MessagerConnectorConfig.update - 'messagerConnectorConfig' can not be null");
        }
        this.getHibernateTemplate().update(messagerConnectorConfig);
    }

}