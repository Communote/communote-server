package com.communote.server.persistence.security.iprange;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.security.iprange.IpRangeFilter</code>.
 * </p>
 * 
 * @see com.communote.server.model.security.IpRangeFilter
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class IpRangeFilterDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.security.iprange.IpRangeFilterDao {

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#count()
     */
    public int count() {
        try {
            return this.handleCount();
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.security.iprange.IpRangeFilterDao.count()' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#create(com.communote.server.model.security.IpRangeFilter)
     */
    public com.communote.server.model.security.IpRangeFilter create(
            com.communote.server.model.security.IpRangeFilter ipRangeFilter) {
        return (com.communote.server.model.security.IpRangeFilter) this.create(TRANSFORM_NONE,
                ipRangeFilter);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#create(int transform,
     *      com.communote.server.persistence.security.iprange.IpRangeFilter)
     */
    public Object create(final int transform,
            final com.communote.server.model.security.IpRangeFilter ipRangeFilter) {
        if (ipRangeFilter == null) {
            throw new IllegalArgumentException(
                    "IpRangeFilter.create - 'ipRangeFilter' can not be null");
        }
        this.getHibernateTemplate().save(ipRangeFilter);
        return this.transformEntity(transform, ipRangeFilter);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.security.iprange.IpRangeFilter>)
     */
    public java.util.Collection<com.communote.server.model.security.IpRangeFilter> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.security.IpRangeFilter> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("IpRangeFilter.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.security.IpRangeFilter>() {
                            public com.communote.server.model.security.IpRangeFilter doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.security.IpRangeFilter> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#create(java.util.
     *      Collection<com.communote.server.persistence.security.iprange.IpRangeFilter>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.security.IpRangeFilter> create(
            final java.util.Collection<com.communote.server.model.security.IpRangeFilter> entities) {
        return (java.util.Collection<com.communote.server.model.security.IpRangeFilter>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.security.IpRangeFilter entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#getIpFilters(int,
     *      int)
     */
    public java.util.List<com.communote.server.persistence.security.iprange.IpRangeFilterVO> getIpFilters(
            final int offset, final int maxCount) {
        try {
            return this.handleGetIpFilters(offset, maxCount);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.security.iprange.IpRangeFilterDao.getIpFilters(int offset, int maxCount)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #count()}
     */
    protected abstract int handleCount();

    /**
     * Performs the core logic for {@link #getIpFilters(int, int)}
     */
    protected abstract java.util.List<com.communote.server.persistence.security.iprange.IpRangeFilterVO> handleGetIpFilters(
            int offset, int maxCount);

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("IpRangeFilter.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.security.IpRangeFilterImpl.class, id);
        return transformEntity(transform,
                (com.communote.server.model.security.IpRangeFilter) entity);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#load(Long)
     */
    public com.communote.server.model.security.IpRangeFilter load(Long id) {
        return (com.communote.server.model.security.IpRangeFilter) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.security.IpRangeFilter> loadAll() {
        return (java.util.Collection<com.communote.server.model.security.IpRangeFilter>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.security.IpRangeFilterImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#remove(com.communote.server.model.security.IpRangeFilter)
     */
    public void remove(com.communote.server.model.security.IpRangeFilter ipRangeFilter) {
        if (ipRangeFilter == null) {
            throw new IllegalArgumentException(
                    "IpRangeFilter.remove - 'ipRangeFilter' can not be null");
        }
        this.getHibernateTemplate().delete(ipRangeFilter);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#remove(java.util.
     *      Collection<com.communote.server.persistence.security.iprange.IpRangeFilter>)
     */
    public void remove(
            java.util.Collection<com.communote.server.model.security.IpRangeFilter> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("IpRangeFilter.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("IpRangeFilter.remove - 'id' can not be null");
        }
        com.communote.server.model.security.IpRangeFilter entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.security.IpRangeFilter)} method. This
     * method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.security.iprange.IpRangeFilterDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.security.IpRangeFilter)
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
     * <code>com.communote.server.persistence.security.iprange.IpRangeFilterDao</code>, please note
     * that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself
     * will be returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.security.iprange.IpRangeFilterDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.security.IpRangeFilter entity) {
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
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#update(com.communote.server.model.security.IpRangeFilter)
     */
    public void update(com.communote.server.model.security.IpRangeFilter ipRangeFilter) {
        if (ipRangeFilter == null) {
            throw new IllegalArgumentException(
                    "IpRangeFilter.update - 'ipRangeFilter' can not be null");
        }
        this.getHibernateTemplate().update(ipRangeFilter);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeFilterDao#update(java.util.
     *      Collection<com.communote.server.persistence.security.iprange.IpRangeFilter>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.security.IpRangeFilter> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("IpRangeFilter.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.security.IpRangeFilter>() {
                            public com.communote.server.model.security.IpRangeFilter doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.security.IpRangeFilter> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}