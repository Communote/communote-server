package com.communote.server.persistence.config;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.config.ApplicationConfigurationSetting</code>.
 * </p>
 * 
 * @see com.communote.server.model.config.ApplicationConfigurationSetting
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ApplicationConfigurationSettingDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.config.ApplicationConfigurationSettingDao {

    /**
     * @see com.communote.server.persistence.config.ApplicationConfigurationSettingDao#create(com.communote.server.model.config.ApplicationConfigurationSetting)
     */
    public com.communote.server.model.config.ApplicationConfigurationSetting create(
            com.communote.server.model.config.ApplicationConfigurationSetting applicationConfigurationSetting) {
        return (com.communote.server.model.config.ApplicationConfigurationSetting) this.create(
                TRANSFORM_NONE, applicationConfigurationSetting);
    }

    /**
     * @see com.communote.server.persistence.config.ApplicationConfigurationSettingDao#create(int
     *      transform, com.communote.server.persistence.config.ApplicationConfigurationSetting)
     */
    public Object create(
            final int transform,
            final com.communote.server.model.config.ApplicationConfigurationSetting applicationConfigurationSetting) {
        if (applicationConfigurationSetting == null) {
            throw new IllegalArgumentException(
                    "ApplicationConfigurationSetting.create - 'applicationConfigurationSetting' can not be null");
        }
        this.getHibernateTemplate().save(applicationConfigurationSetting);
        return this.transformEntity(transform, applicationConfigurationSetting);
    }

    /**
     * @see com.communote.server.persistence.config.ApplicationConfigurationSettingDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.config.
     *      ApplicationConfigurationSetting>)
     */
    public java.util.Collection<com.communote.server.model.config.ApplicationConfigurationSetting> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.config.ApplicationConfigurationSetting> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ApplicationConfigurationSetting.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.ApplicationConfigurationSetting>() {
                            public com.communote.server.model.config.ApplicationConfigurationSetting doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.ApplicationConfigurationSetting> entityIterator = entities
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
     *      com.communote.server.persistence.config.ApplicationConfigurationSettingDao#create(java.util
     *      .Collection<com.communote.server.persistence.config.ApplicationConfigurationSetting>)
     */
    public java.util.Collection<com.communote.server.model.config.ApplicationConfigurationSetting> create(
            final java.util.Collection<com.communote.server.model.config.ApplicationConfigurationSetting> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.config.ApplicationConfigurationSetting entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.config.ApplicationConfigurationSettingDao#load(int,
     *      String)
     */
    public Object load(final int transform, final String settingKey) {
        if (settingKey == null) {
            throw new IllegalArgumentException(
                    "ApplicationConfigurationSetting.load - 'settingKey' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.config.ApplicationConfigurationSettingImpl.class,
                settingKey);
        return transformEntity(transform,
                (com.communote.server.model.config.ApplicationConfigurationSetting) entity);
    }

    /**
     * @see com.communote.server.persistence.config.ApplicationConfigurationSettingDao#load(String)
     */
    public com.communote.server.model.config.ApplicationConfigurationSetting load(String settingKey) {
        return (com.communote.server.model.config.ApplicationConfigurationSetting) this.load(
                TRANSFORM_NONE, settingKey);
    }

    /**
     * @see com.communote.server.persistence.config.ApplicationConfigurationSettingDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.config.ApplicationConfigurationSetting> loadAll() {
        return (java.util.Collection<com.communote.server.model.config.ApplicationConfigurationSetting>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.config.ApplicationConfigurationSettingDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.config.ApplicationConfigurationSettingImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.config.ApplicationConfigurationSettingDao#remove(com.communote.server.model.config.ApplicationConfigurationSetting)
     */
    public void remove(
            com.communote.server.model.config.ApplicationConfigurationSetting applicationConfigurationSetting) {
        if (applicationConfigurationSetting == null) {
            throw new IllegalArgumentException(
                    "ApplicationConfigurationSetting.remove - 'applicationConfigurationSetting' can not be null");
        }
        this.getHibernateTemplate().delete(applicationConfigurationSetting);
    }

    /**
     * @see 
     *      com.communote.server.persistence.config.ApplicationConfigurationSettingDao#remove(java.util
     *      .Collection<com.communote.server.persistence.config.ApplicationConfigurationSetting>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.config.ApplicationConfigurationSetting> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ApplicationConfigurationSetting.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.config.ApplicationConfigurationSettingDao#remove(String)
     */
    public void remove(String settingKey) {
        if (settingKey == null) {
            throw new IllegalArgumentException(
                    "ApplicationConfigurationSetting.remove - 'settingKey' can not be null");
        }
        com.communote.server.model.config.ApplicationConfigurationSetting entity = this
                .load(settingKey);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.config.ApplicationConfigurationSetting)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.config.ApplicationConfigurationSettingDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.config.ApplicationConfigurationSetting)
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
     * <code>com.communote.server.persistence.config.ApplicationConfigurationSettingDao</code>,
     * please note that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the
     * entity itself will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.config.ApplicationConfigurationSettingDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.config.ApplicationConfigurationSetting entity) {
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
     * @see com.communote.server.persistence.config.ApplicationConfigurationSettingDao#update(com.communote.server.model.config.ApplicationConfigurationSetting)
     */
    public void update(
            com.communote.server.model.config.ApplicationConfigurationSetting applicationConfigurationSetting) {
        if (applicationConfigurationSetting == null) {
            throw new IllegalArgumentException(
                    "ApplicationConfigurationSetting.update - 'applicationConfigurationSetting' can not be null");
        }
        this.getHibernateTemplate().update(applicationConfigurationSetting);
    }

    /**
     * @see 
     *      com.communote.server.persistence.config.ApplicationConfigurationSettingDao#update(java.util
     *      .Collection<com.communote.server.persistence.config.ApplicationConfigurationSetting>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.config.ApplicationConfigurationSetting> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "ApplicationConfigurationSetting.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.config.ApplicationConfigurationSetting>() {
                            public com.communote.server.model.config.ApplicationConfigurationSetting doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.config.ApplicationConfigurationSetting> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}