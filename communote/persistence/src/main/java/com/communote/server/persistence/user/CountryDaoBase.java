package com.communote.server.persistence.user;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.Country</code>.
 * </p>
 * 
 * @see com.communote.server.model.user.Country
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class CountryDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.CountryDao {

    /**
     * @see com.communote.server.persistence.user.CountryDao#create(com.communote.server.model.user.Country)
     */
    public com.communote.server.model.user.Country create(
            com.communote.server.model.user.Country country) {
        return (com.communote.server.model.user.Country) this.create(TRANSFORM_NONE, country);
    }

    /**
     * @see com.communote.server.persistence.user.CountryDao#create(int transform,
     *      com.communote.server.persistence.user.Country)
     */
    public Object create(final int transform, final com.communote.server.model.user.Country country) {
        if (country == null) {
            throw new IllegalArgumentException("Country.create - 'country' can not be null");
        }
        this.getHibernateTemplate().save(country);
        return this.transformEntity(transform, country);
    }

    /**
     * @see com.communote.server.persistence.user.CountryDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.Country>)
     */
    public java.util.Collection<com.communote.server.model.user.Country> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.user.Country> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Country.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.Country>() {
                            public com.communote.server.model.user.Country doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.Country> entityIterator = entities
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
     *      com.communote.server.persistence.user.CountryDao#create(java.util.Collection<de.communardo
     *      .kenmei.core.api.bo.user.Country>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.Country> create(
            final java.util.Collection<com.communote.server.model.user.Country> entities) {
        return (java.util.Collection<com.communote.server.model.user.Country>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.user.Country entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.CountryDao#findCountryByCode(String)
     */
    public com.communote.server.model.user.Country findCountryByCode(final String countryCode) {
        if (countryCode == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.CountryDao.findCountryByCode(String countryCode) - 'countryCode' can not be null");
        }
        try {
            return this.handleFindCountryByCode(countryCode);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.CountryDao.findCountryByCode(String countryCode)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #findCountryByCode(String)}
     */
    protected abstract com.communote.server.model.user.Country handleFindCountryByCode(
            String countryCode);

    /**
     * @see com.communote.server.persistence.user.CountryDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Country.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.CountryImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.user.Country) entity);
    }

    /**
     * @see com.communote.server.persistence.user.CountryDao#load(Long)
     */
    public com.communote.server.model.user.Country load(Long id) {
        return (com.communote.server.model.user.Country) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.CountryDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.Country> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.Country>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.CountryDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.CountryImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.CountryDao#remove(com.communote.server.model.user.Country)
     */
    public void remove(com.communote.server.model.user.Country country) {
        if (country == null) {
            throw new IllegalArgumentException("Country.remove - 'country' can not be null");
        }
        this.getHibernateTemplate().delete(country);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.CountryDao#remove(java.util.Collection<de.communardo
     *      .kenmei.core.api.bo.user.Country>)
     */
    public void remove(java.util.Collection<com.communote.server.model.user.Country> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Country.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.CountryDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Country.remove - 'id' can not be null");
        }
        com.communote.server.model.user.Country entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.Country)} method. This method
     * does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.CountryDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.Country)
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
     * <code>com.communote.server.persistence.user.CountryDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.CountryDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.Country entity) {
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
     * @see com.communote.server.persistence.user.CountryDao#update(com.communote.server.model.user.Country)
     */
    public void update(com.communote.server.model.user.Country country) {
        if (country == null) {
            throw new IllegalArgumentException("Country.update - 'country' can not be null");
        }
        this.getHibernateTemplate().update(country);
    }

    /**
     * @see 
     *      com.communote.server.persistence.user.CountryDao#update(java.util.Collection<de.communardo
     *      .kenmei.core.api.bo.user.Country>)
     */
    public void update(final java.util.Collection<com.communote.server.model.user.Country> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Country.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.Country>() {
                            public com.communote.server.model.user.Country doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.Country> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}