package com.communote.server.persistence.security.iprange;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.security.iprange.IpRange</code>.
 * </p>
 * 
 * @see com.communote.server.model.security.IpRange
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class IpRangeDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.security.iprange.IpRangeDao {

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeDao#create(com.communote.server.model.security.IpRange)
     */
    public com.communote.server.model.security.IpRange create(
            com.communote.server.model.security.IpRange ipRange) {
        return (com.communote.server.model.security.IpRange) this.create(TRANSFORM_NONE, ipRange);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeDao#create(int transform,
     *      com.communote.server.persistence.security.iprange.IpRange)
     */
    public Object create(final int transform,
            final com.communote.server.model.security.IpRange ipRange) {
        if (ipRange == null) {
            throw new IllegalArgumentException("IpRange.create - 'ipRange' can not be null");
        }
        this.getHibernateTemplate().save(ipRange);
        return this.transformEntity(transform, ipRange);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.security.iprange.IpRange>)
     */
    public java.util.Collection<com.communote.server.model.security.IpRange> create(
            final int transform,
            final java.util.Collection<com.communote.server.model.security.IpRange> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("IpRange.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.security.IpRange>() {
                            public com.communote.server.model.security.IpRange doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.security.IpRange> entityIterator = entities
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
     *      com.communote.server.persistence.security.iprange.IpRangeDao#create(java.util.Collection<
     *      com.communote.server.persistence.security.iprange.IpRange>)
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.security.IpRange> create(
            final java.util.Collection<com.communote.server.model.security.IpRange> entities) {
        return (java.util.Collection<com.communote.server.model.security.IpRange>) create(
                TRANSFORM_NONE, entities);
    }

    /**
     * {@inheritDoc}
     */
    public void evict(com.communote.server.model.security.IpRange entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeDao#load(int, Long)
     */
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("IpRange.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.security.IpRangeImpl.class, id);
        return transformEntity(transform, (com.communote.server.model.security.IpRange) entity);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeDao#load(Long)
     */
    public com.communote.server.model.security.IpRange load(Long id) {
        return (com.communote.server.model.security.IpRange) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeDao#loadAll()
     */
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.security.IpRange> loadAll() {
        return (java.util.Collection<com.communote.server.model.security.IpRange>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeDao#loadAll(int)
     */
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.security.IpRangeImpl.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeDao#remove(com.communote.server.model.security.IpRange)
     */
    public void remove(com.communote.server.model.security.IpRange ipRange) {
        if (ipRange == null) {
            throw new IllegalArgumentException("IpRange.remove - 'ipRange' can not be null");
        }
        this.getHibernateTemplate().delete(ipRange);
    }

    /**
     * @see 
     *      com.communote.server.persistence.security.iprange.IpRangeDao#remove(java.util.Collection<
     *      com.communote.server.persistence.security.iprange.IpRange>)
     */
    public void remove(java.util.Collection<com.communote.server.model.security.IpRange> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("IpRange.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.security.iprange.IpRangeDao#remove(Long)
     */
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("IpRange.remove - 'id' can not be null");
        }
        com.communote.server.model.security.IpRange entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.security.IpRange)} method. This method
     * does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.security.iprange.IpRangeDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.security.IpRange)
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
     * <code>com.communote.server.persistence.security.iprange.IpRangeDao</code>, please note that
     * the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * 
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.security.iprange.IpRangeDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.security.IpRange entity) {
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
     * @see com.communote.server.persistence.security.iprange.IpRangeDao#update(com.communote.server.model.security.IpRange)
     */
    public void update(com.communote.server.model.security.IpRange ipRange) {
        if (ipRange == null) {
            throw new IllegalArgumentException("IpRange.update - 'ipRange' can not be null");
        }
        this.getHibernateTemplate().update(ipRange);
    }

    /**
     * @see 
     *      com.communote.server.persistence.security.iprange.IpRangeDao#update(java.util.Collection<
     *      com.communote.server.persistence.security.iprange.IpRange>)
     */
    public void update(
            final java.util.Collection<com.communote.server.model.security.IpRange> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("IpRange.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.security.IpRange>() {
                            public com.communote.server.model.security.IpRange doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.security.IpRange> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

}