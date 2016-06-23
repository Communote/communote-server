package com.communote.server.persistence.config;

import com.communote.server.model.config.Setting;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.config.Setting</code>.
 * </p>
 *
 * @see Setting
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class SettingDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.config.SettingDao {

    /**
     * @see com.communote.server.persistence.config.SettingDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.config.Setting>)
     */
    @Override
    public java.util.Collection<Setting> create(final int transform,
            final java.util.Collection<Setting> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Setting.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Setting>() {
                    @Override
                    public Setting doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<Setting> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            create(transform, entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.config.SettingDao#create(int transform,
     *      com.communote.server.persistence.config.Setting)
     */
    @Override
    public Object create(final int transform, final Setting setting) {
        if (setting == null) {
            throw new IllegalArgumentException("Setting.create - 'setting' can not be null");
        }
        this.getHibernateTemplate().save(setting);
        return this.transformEntity(transform, setting);
    }

    /**
     * @see 
     *      com.communote.server.persistence.config.SettingDao#create(java.util.Collection<de.communardo
     *      .kenmei.core.api.bo.config.Setting>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<Setting> create(final java.util.Collection<Setting> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see com.communote.server.persistence.config.SettingDao#create(Setting)
     */
    @Override
    public Setting create(Setting setting) {
        return (Setting) this.create(TRANSFORM_NONE, setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(Setting entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.config.SettingDao#load(int, String)
     */
    @Override
    public Object load(final int transform, final String settingKey) {
        if (settingKey == null) {
            throw new IllegalArgumentException("Setting.load - 'settingKey' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(Setting.class, settingKey);
        return transformEntity(transform, (Setting) entity);
    }

    /**
     * @see com.communote.server.persistence.config.SettingDao#load(String)
     */
    @Override
    public Setting load(String settingKey) {
        return (Setting) this.load(TRANSFORM_NONE, settingKey);
    }

    /**
     * @see com.communote.server.persistence.config.SettingDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<Setting> loadAll() {
        return (java.util.Collection<Setting>) this.loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.config.SettingDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(Setting.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see 
     *      com.communote.server.persistence.config.SettingDao#remove(java.util.Collection<de.communardo
     *      .kenmei.core.api.bo.config.Setting>)
     */
    @Override
    public void remove(java.util.Collection<Setting> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Setting.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.config.SettingDao#remove(Setting)
     */
    @Override
    public void remove(Setting setting) {
        if (setting == null) {
            throw new IllegalArgumentException("Setting.remove - 'setting' can not be null");
        }
        this.getHibernateTemplate().delete(setting);
    }

    /**
     * @see com.communote.server.persistence.config.SettingDao#remove(String)
     */
    @Override
    public void remove(String settingKey) {
        if (settingKey == null) {
            throw new IllegalArgumentException("Setting.remove - 'settingKey' can not be null");
        }
        Setting entity = this.load(settingKey);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,Setting)} method.
     * This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.config.SettingDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,Setting)
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
     * <code>com.communote.server.persistence.config.SettingDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.config.SettingDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform, final Setting entity) {
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
     * @see 
     *      com.communote.server.persistence.config.SettingDao#update(java.util.Collection<de.communardo
     *      .kenmei.core.api.bo.config.Setting>)
     */
    @Override
    public void update(final java.util.Collection<Setting> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Setting.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Setting>() {
                    @Override
                    public Setting doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException {
                        for (java.util.Iterator<Setting> entityIterator = entities.iterator(); entityIterator
                                .hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

    /**
     * @see com.communote.server.persistence.config.SettingDao#update(Setting)
     */
    @Override
    public void update(Setting setting) {
        if (setting == null) {
            throw new IllegalArgumentException("Setting.update - 'setting' can not be null");
        }
        this.getHibernateTemplate().update(setting);
    }

}